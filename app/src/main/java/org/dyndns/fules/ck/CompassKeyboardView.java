package org.dyndns.fules.ck;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.inputmethodservice.KeyboardView;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.InputType;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.SoundEffectConstants;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

interface EmbeddableItem {
	public void calculateSizes();
}

/*
 * <Action> tag
 */
class Action {
	int		keyCode, layout;
	String		code, text, cmd, handlerStr;
	boolean		isLock, isEmpty, isSpecial;

	public Action() {
		isLock = isSpecial = false;
		keyCode = layout = -1;
		isEmpty = true;
	}

	public Action(KbdModel.Dir dir) throws IOException {
		isLock = isEmpty = false;
		keyCode = layout = -1;
		text=dir.show;
		if(dir.actType==0) isEmpty=true;


		switch (dir.actType) {
			case 1:code=dir.sValue;break;
			case 2:keyCode=dir.iValue;break;
			case 3:handlerStr=dir.sValue;break;
		}
	}
}

/*
 * <Layout> tag
 */
public class CompassKeyboardView extends FrameLayout {
	// Constants
	private static final String		TAG = "CompassKeyboard";
	private static final long[][]		vibratePattern = { { 10, 10 }, { 10, 10, 10, 10 } };
	private static final int		LONG_TAP_TIMEOUT = 700;	// timeout in msec after which a tap is considered a long one
	private static final int		LONG_TAP_REPEAT = 75;
	public static final int			NONE	= -1;
	public static final int			NW	= 0;
	public static final int			N	= 1;
	public static final int			NE	= 2;
	public static final int			W	= 3;
	public static final int			TAP	= 4;
	public static final int			E	= 5;
	public static final int			SW	= 6;
	public static final int			S	= 7;
	public static final int			SE	= 8;

	public static final int			FEEDBACK_HIGHLIGHT	= 1;
	public static final int			FEEDBACK_TOAST		= 2;

	// Parameters
	int					vibrateOnKey = 0;
	int					vibrateOnModifier = 0;
	int					vibrateOnCancel = 0;
	int					feedbackNormal = 0;
	int					feedbackPassword = 0;
	float					keyMM = 40;	// maximal key size in mm-s
	float					marginLeft = 0, marginRight = 0, marginBottom = 0; // margins in mm-s

	// Internal params
	int					nColumns;	// maximal number of symbol columns (eg. 3 for full key, 2 for side key), used for size calculations
	int					nKeys;		// maximal number of keys per row, used for calculating with the gaps between keys
	int					sym, gap;	// size of symbols on keys and gap between them (in pixels)
	float					fontSize;	// height of the key caption font in pixels
	float					fontDispY;	// Y-displacement of key caption font (top to baseline)
	boolean					isTypingPassword; // is the user typing a password
	String lang = "none";

	Vibrator				vibro;		// vibrator
	Paint					textPaint;	// Paint for drawing key captions
	Paint					specPaint;	// Paint for drawing special key captions
	Paint					candidatePaint;	// Paint for drawing candidate key captions
	OnKeyboardActionListener	actionListener;	// owner of the callback methods, result is passed to this instance
	HashSet<String>				modifiers;	// currently active modifiers
	HashSet<String>				locks;		// currently active locks
	HashSet<String>				effectiveMods;	// currently active effective modifiers (== symmetric difference of modifiers and locks)
	LinearLayout.LayoutParams		lp;		// layout params for placing the rows
	LinearLayout				kbd;  		// the keyboard layer
	OverlayView				overlay;	// the overlay layer
	CompassKeyboard ck;
	LanguageHandler languageHandler;
	LongTap					onLongTap;	// long tap checker
	Row.Key					longTapKey;
	boolean					wasLongTap;	// marker to skip processing the release of a long tap
	//Toast					toast;
	float[]					downX={0,0,0,0}, downY={0,0,0,0}, upX={0,0,0,0}, upY={0,0,0,0};	// the coordinates of a swipe, used for recognising global swipes

	/*
	 * Long tap handler
	 */
	private final class LongTap implements Runnable {
		public void run() {
			wasLongTap=true;
			processAction(longTapKey.dir[TAP]);
			postDelayed(onLongTap, LONG_TAP_REPEAT);
		}
	}

	/*
	 * <Row> tag
	 */
	class Row extends LinearLayout implements EmbeddableItem {
		int	ymax;					// height of the row
		int	y1, y2, y3;				// y positions of the symbol rows within the keys
		int	columns;				// number of symbol columns (eg. 3 for full key, 2 for side key), used for size calculations
		Paint	buttonPaint;				// Paint used for drawing the key background
		Paint	framePaint;				// Paint used for drawing the key frame
		boolean	hasTop, hasBottom;			// does all the keys in the row have tops and bottoms?

		/*
		 * <Key> tag
		 */
		class Key extends View implements EmbeddableItem {
			RectF			fullRect;		// the rectangle of the key frame
			RectF			innerRect;		// the rectangle of the key body
			int			xmax;			// width of the key
			int			x1, x2, x3;		// x positions of the symbol columns within the key
			boolean			hasLeft, hasRight;	// does the key have the given left and right symbols?
			boolean			hasTop, hasBottom;	// does the key have tops and bottoms? (NOTE: can only be stricter than the row!)
			int			candidateDir=NONE;		// the direction into which a drag is in progress, or NONE if inactive
			Action[] dir;

			/*
			 * Methods of Key
			 */

			public Key(Context context, KbdModel.Col col) throws IOException {
				super(context);
				dir=new Action[9];
				String s;

				hasLeft = hasRight = hasTop = hasBottom = true;
				for(int i=0;i<9;i++)
					dir[i]=new Action(col.dir[i]);

				// delete empty entries
				for (int i = 0; i < 9; i++) {
					if(dir[i]!=null)
						if(dir[i].isEmpty)
							dir[i] = null;
				}

			}

			// Recalculate the drawing coordinates according to the symbol size
			public void calculateSizes() {
				xmax	= Math.round(4 * gap + 3    * sym);
				x1	= Math.round(    gap + 0.5f * sym);
				x2	= Math.round(2 * gap + 1.5f * sym);
				x3	= Math.round(3 * gap + 2.5f * sym);
				fullRect = new RectF(0, 0, xmax - 1, ymax - 1);
				innerRect = new RectF(2, 2, xmax - 3, ymax - 3);
			}

			void setCandidate(int d) {
				candidateDir = d;
				invalidate();
			}

			// figure out the direction of the swipe
			int getDirection(float x, float y,int idx) {
				int d;
				float dx = (x - downX[idx]) * 3;
				float dy = (y - downY[idx]) * 3;

				if (dx < -xmax) {
					if (dy < -ymax)
						d = NW;
					else if (dy < ymax)
						d = W;
					else
						d = SW;
				}
				else if (dx < xmax) {
					if (dy < -ymax)
						d = N;
					else if (dy < ymax)
						d = TAP;
					else
						d = S;
				}
				else {
					if (dy < -ymax)
						d = NE;
					else if (dy < ymax)
						d = E;
					else
						d = SE;
				}

				return d;
			}

			// Touch event handler
			@Override public boolean onTouchEvent(MotionEvent event) {
				int idx=event.getActionIndex();
				if(idx>3) return false;
				boolean res = processTouchEvent(event);
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						longTapKey = this;
					case MotionEvent.ACTION_POINTER_DOWN:
						setCandidate(getDirection(event.getX(), event.getY(),idx));
						return true;

					case MotionEvent.ACTION_POINTER_UP:
					case MotionEvent.ACTION_UP:
						setCandidate(NONE);

						// if the key is not valid in this state or there is no corresponding Action for it, then release the modifiers
						if(!wasLongTap)processAction(dir[getDirection(upX[idx], upY[idx],idx)]);
						else wasLongTap=false;
						// touch event processed
						return true;

					case MotionEvent.ACTION_MOVE:
						setCandidate(getDirection(event.getX(), event.getY(),idx));
						break;
				}
				return res;
			}

			// Draw a Action symbol label if it is specified
			protected void drawLabel(Canvas canvas, int d, int x, int y) {
				if (dir[d] != null)
					canvas.drawText(dir[d].text, x, y,
							(d == candidateDir) ? candidatePaint : dir[d].isSpecial ? specPaint : textPaint);
			}

			// Redraw the key
			@Override protected void onDraw(Canvas canvas) {
				// draw the background
				canvas.drawRoundRect(fullRect, 0, 0, framePaint);
				canvas.drawRoundRect(innerRect, 0, 0, buttonPaint);
				//canvas.drawRoundRect(fullRect, 2, 2, buttonPaint);	// for a plain one-gradient background

				// draw the Action labels if the key is valid in this state
					drawLabel(canvas, NW,  x1, y1);
					drawLabel(canvas, N,   x2, y1);
					drawLabel(canvas, NE,  x3, y1);

					drawLabel(canvas, W,   x1, y2);
					drawLabel(canvas, TAP, x2, y2);
					drawLabel(canvas, E,   x3, y2);

					drawLabel(canvas, SW,  x1, y3);
					drawLabel(canvas, S,   x2, y3);
					drawLabel(canvas, SE,  x3, y3);
			}

			// Report the size of the key
			@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				// policy: if not specified by the parent as EXACTLY, use our own ideas
				int w = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ? MeasureSpec.getSize(widthMeasureSpec) : xmax;
				int h = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ? MeasureSpec.getSize(heightMeasureSpec) : ymax;
				setMeasuredDimension(w, h);
			}
		}

		/*
		 * Methods of Row
		 */

		public Row(Context context, KbdModel.Row row) throws IOException {
			super(context);
			String s;

			setOrientation(LinearLayout.HORIZONTAL);
			setGravity(Gravity.CENTER);

			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, 0, 0);

			buttonPaint = new Paint();
			buttonPaint.setAntiAlias(true);
			buttonPaint.setColor(Color.WHITE);

			framePaint = new Paint();
			framePaint.setAntiAlias(true);
			framePaint.setColor(Color.LTGRAY);

			columns = 0;
			for(int i=0;i<row.col.length;i++) {
				Key nk = new Key(getContext(), row.col[i]);
				addView(nk,lp);
				columns+=3;
			}

		}

		// Recalculate the drawing coordinates according to the symbol size
		public void calculateSizes() {
			ymax	= Math.round(2 * gap + 3 * fontSize);
			y1	= Math.round(    gap +                fontDispY);
			y2	= Math.round(    gap + 1 * fontSize + fontDispY);
			y3	= Math.round(    gap + 2 * fontSize + fontDispY);

			// Set the key background color
			buttonPaint.setShader(new LinearGradient(0, 0, 0, ymax, 0xffffffff, 0xffeeeeee, TileMode.CLAMP));
			framePaint.setShader(new LinearGradient(0, 0, 0, ymax, 0xffdddddd, 0xffcccccc, TileMode.CLAMP));

			int n = getChildCount();
			for (int i = 0; i < n; i++) {
				EmbeddableItem e = (EmbeddableItem)getChildAt(i);
				e.calculateSizes();
			}
		}

		// Report the size of the row
		@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// if the parent specified only a maximal width, make the row span the whole of it
			if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(
						MeasureSpec.EXACTLY,
						MeasureSpec.getSize(widthMeasureSpec));

			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	/*
	 * Methods of OverlayView
	 */

	class OverlayView extends View {
		int dir = NONE;

		public OverlayView(Context context) {
			super(context);
		}

		@Override protected void onDraw(Canvas canvas) {
			int w = canvas.getWidth();
			int h = canvas.getHeight();
			switch (dir) {
				case NW:
				case SE:
					canvas.drawLine(0, 0, w, h, candidatePaint);
					break;

				case N:
				case S:
					int cx = w / 2;
					canvas.drawLine(cx, 0, cx, h, candidatePaint);
					break;

				case NE:
				case SW:
					canvas.drawLine(w, 0, 0, h, candidatePaint);
					break;

				case W:
				case E:
					int cy = h / 2;
					canvas.drawLine(0, cy, w, cy, candidatePaint);
					break;
			}
		}

		@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(kbd.getWidth(), kbd.getHeight());
		}

		void setDir(int d) {
			dir = d;
			invalidate();
		}
	}

	/*
	 * Methods of CompassKeyboardView
	 */

	public CompassKeyboardView(CompassKeyboard context) {
		super(context);
		languageHandler = new KoreanHandler();
		ck=context;
		kbd = new LinearLayout(context);
		kbd.setOrientation(LinearLayout.VERTICAL);
		kbd.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
		//kbd.setBackgroundColor(0xff003f00); // for debugging placement
		addView(kbd);

		overlay = new OverlayView(context);
		addView(overlay);

		lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 0, 0, 0);

		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.DKGRAY);
		textPaint.setTextAlign(Paint.Align.CENTER);
		//textPaint.setShadowLayer(3, 0, 2, 0xff000000);

		specPaint = new Paint();
		specPaint.setAntiAlias(true);
		specPaint.setColor(Color.BLUE);
		specPaint.setTextAlign(Paint.Align.CENTER);
		//specPaint.setShadowLayer(3, 0, 2, 0xff000000);

		candidatePaint = new Paint();
		candidatePaint.setAntiAlias(true);
		candidatePaint.setColor(Color.BLACK);
		candidatePaint.setTextAlign(Paint.Align.CENTER);
		candidatePaint.setShadowLayer(3, 0, 2, 0xff00ffff);

		setSoundEffectsEnabled(true);
		vibro = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		onLongTap = new LongTap();
		modifiers = new HashSet();
		locks = new HashSet();
		effectiveMods = new HashSet();

		//toast = Toast.makeText(context, "<none>", Toast.LENGTH_SHORT);
		//toast.setGravity(Gravity.BOTTOM, 0, 0);
	}

	void vibrateCode(int n) {
		if (n == -1) {
			playSoundEffect(SoundEffectConstants.CLICK);
		}
		else if ((n >= 0) && (n < vibratePattern.length))
			vibro.vibrate(vibratePattern[n], -1);
	}

	// Read the layout from an XML parser
	public void readLayout(KbdModel kbdModel) throws IOException {
		//lang=kbdModel.kbdLang;
		// drop and re-read all previously existing rows
		kbd.removeAllViews();
		nColumns = nKeys = 0;
		for(int i=0;i<kbdModel.row.length;i++) {
				Row nr = new Row(getContext(), kbdModel.row[i]);
			kbd.addView(nr, lp);

			int nc = nr.getChildCount();
				if (nColumns < nr.columns)
					nColumns = nr.columns;
				if (nKeys < nc)
					nKeys = nc;
			/*else if (parser.getName().contentEquals("Align")) {
				Align na = new Align(getContext(), parser);
				kbd.addView(na, lp);

				if (nColumns < na.width)
					nColumns = na.width;
			}*/
		}


		// recalculate sizes and set bg colour
		calculateSizesForMetrics(getResources().getDisplayMetrics());
	}

	// Recalculate all the sizes according to the display metrics
	public void calculateSizesForMetrics(DisplayMetrics metrics) {
		// note: the metrics may change during the lifetime of the instance, so these precalculations could not be done in the constructor
		int i, totalWidth;
		int marginPixelsLeft = Math.round(marginLeft * metrics.xdpi / 25.4f);
		int marginPixelsRight = Math.round(marginRight * metrics.xdpi / 25.4f);
		int marginPixelsBottom = Math.round(marginBottom * metrics.ydpi / 25.4f);
		setPadding(marginPixelsLeft, 0, marginPixelsRight, marginPixelsBottom);

		// Desired "key size" in pixels is keyMM * metrics.xdpi / 25.4f
		// This "key size" is an abstraction of a key that has 3 symbol columns (and therefore 4 gaps: gSgSgSg),
		// so that the gaps take up 1/3 and the symbols take up 2/3 of the key, so
		//   4*gaps = 1/3 * keySize	-> gap = keySize / 12
		//   3*sym = 2/3 * keySize	-> sym = 2 * keySize / 9
		// We have nKeys keys and nColumns columns, that means nKeys*gap + nColumns*(sym + gap), that is
		//   nKeys*keySize/12 + nColumns*keySize*(2/9 + 1/12) = keySize * (nKeys/12 + nColumns*11/36)
		totalWidth = Math.round(keyMM * metrics.xdpi / 25.4f * ((nKeys / 12.f) + (nColumns * 11 / 36.f)));
		// Regardless of keyMM, it must fit the metrics, that is width - margins - 1 pixel between keys
		i = metrics.widthPixels - marginPixelsLeft - marginPixelsRight - (nKeys - 1);
		if (i < totalWidth)
			totalWidth = i;

		// Now back to real key sizes, we still have nKeys keys and nColumns columns for these totalWidth pixels, which means 
		//   nKeys*gap + nColumns*(sym + gap) = gap*(nKeys+nColumns) + sym*nColumns <= totalWidth

		// Rounding errors can foul up everything, and the sum above is more sensitive on the error of gap than of sym,
		//   so we calculate gap first (with rounding) and then adjust sym to it.
		// As decided, a gap to symbol ratio of 1/3 to 2/3 would be ergonomically pleasing, so 2*4*gap = 3*sym, that is sym = 8*gap/3, so
		//   gap*(nKeys+nColumns) + 8*gap/3*nColumns = totalWidth
		//   gap*(nKeys+nColumns + 8/3*nColumns) = totalWidth
		gap = Math.round(totalWidth / (nKeys+nColumns + 8*nColumns/3.f));
		// Calculating sym as 8/3*gap is tempting, but that wouldn't compensate the rounding error above, so we have to derive
		// it from totalWidth and rounding it only downwards:
		//   gap*(nKeys+nColumns) + sym*nColumns = totalWidth
		sym = (totalWidth - gap*(nKeys+nColumns)) / nColumns;

		// Sample data: nKeys=5, columns=13; Galaxy Mini: 240x320, Ace: 320x480, S: 480x80, S3: 720x1280 

		// construct the Paint used for printing the labels
		textPaint.setTextSize(sym);
		int newSym = sym;

		specPaint.setTextSize(sym);
		candidatePaint.setTextSize(sym * 3 / 2);
		candidatePaint.setStrokeWidth(gap);

		Paint.FontMetrics fm = textPaint.getFontMetrics();
		fontSize = fm.descent - fm.ascent;
		fontDispY = -fm.ascent;

		Log.v(TAG, "keyMM=" + String.valueOf(keyMM) + ", xdpi=" + String.valueOf(metrics.xdpi) + ", ydpi=" + String.valueOf(metrics.ydpi) + ", nKeys=" + String.valueOf(nKeys) + ", nColumns=" + String.valueOf(nColumns) + ", totalWidth=" + String.valueOf(totalWidth) + ", max=" + String.valueOf(i) + ", sym=" + String.valueOf(sym) + ", gap=" + String.valueOf(gap) + ", reqFS="+String.valueOf(sym)+", fs="+String.valueOf(fontSize)+", asc="+String.valueOf(fm.ascent)+", desc="+String.valueOf(fm.descent));

		//toast.setGravity(Gravity.TOP + Gravity.CENTER_HORIZONTAL, 0, -sym);

		int n = kbd.getChildCount();
		for (i = 0; i < n; i++) {
			EmbeddableItem e = (EmbeddableItem)kbd.getChildAt(i);
			e.calculateSizes();
		}
	}

	// Common touch event handler - record coordinates and manage long tap handlers
	public boolean processTouchEvent(MotionEvent event) {
		int idx=event.getActionIndex();
		if(idx>3) return false;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				postDelayed(onLongTap, LONG_TAP_TIMEOUT);
				// remember the swipe starting coordinates for checking for global swipes
				downX[idx] = event.getX();
				downY[idx] = event.getY();
				// register a long tap handler
				wasLongTap = false;
				return true;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
				// end of swipe
				upX[idx] = event.getX();
				upY[idx] = event.getY();
				// cancel any pending checks for long tap
				removeCallbacks(onLongTap);
				// touch event processed
				return true;

			case MotionEvent.ACTION_MOVE:
				// cancel any pending checks for long tap
				removeCallbacks(onLongTap);
				return false;
		}
		// we're not interested in other kinds of events
		return false;
	}
	public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
		actionListener = listener;
	}

	public void setInputType(int type) {
		isTypingPassword = ((type & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) &&
				((type & InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_PASSWORD);
	}

	private boolean processAction(Action cd) {
		//toast.cancel();
		if (cd == null)
			return false;

		if (actionListener != null) {
			if (cd.code != null)
				actionListener.onText(cd.code); // process a 'code'
			else if (cd.keyCode >= 0)
				actionListener.onKey(cd.keyCode, null); // process a 'key'
			else if (cd.handlerStr!=null){
				ck.handle(cd.handlerStr);
			}
			else if (actionListener instanceof CompassKeyboard) {
				CompassKeyboard ck = (CompassKeyboard)actionListener;
				if (cd.layout >= 0)
					ck.updateLayout(cd.layout); // process a 'layout'
				else if ((cd.cmd != null) && (cd.cmd.length() > 0))
					ck.execCmd(cd.cmd); // process a 'cmd'
			}
			vibrateCode(vibrateOnKey);
		}

		return true;
	}

	public void setVibrateOnKey(int n) {
		vibrateOnKey = n;
	}

	public void setVibrateOnModifier(int n) {
		vibrateOnModifier = n;
	}

	public void setVibrateOnCancel(int n) {
		vibrateOnCancel = n;
	}

	public void setFeedbackNormal(int n) {
		feedbackNormal = n;
	}

	public void setFeedbackPassword(int n) {
		feedbackPassword = n;
	}

	public void setLeftMargin(float f) {
		marginLeft = f;
		calculateSizesForMetrics(getResources().getDisplayMetrics());
	}

	public void setRightMargin(float f) {
		marginRight = f;
		calculateSizesForMetrics(getResources().getDisplayMetrics());
	}

	public void setBottomMargin(float f) {
		marginBottom = f;
		calculateSizesForMetrics(getResources().getDisplayMetrics());
	}

	public void setMaxKeySize(float f) {
		keyMM = f > 0 ? f : 12;
		calculateSizesForMetrics(getResources().getDisplayMetrics());
	}
}

// vim: set ai si sw=8 ts=8 noet:
