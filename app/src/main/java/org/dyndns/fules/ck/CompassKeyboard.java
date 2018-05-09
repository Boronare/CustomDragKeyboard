package org.dyndns.fules.ck;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.ExtractedTextRequest;
import android.R.id;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.textservice.TextInfo;
import android.view.textservice.SpellCheckerSession;
public class CompassKeyboard extends InputMethodService implements OnKeyboardActionListener, SharedPreferences.OnSharedPreferenceChangeListener  {
	public static final String	SHARED_PREFS_NAME = "CompassKeyboardSettings";
	public static final int[]	builtinLayouts = { R.xml.default_latin, R.xml.default_cyrillic, R.xml.default_greek }; // keep in sync with constants.xml
	private static final String	TAG = "CompassKeyboard";
	private SharedPreferences	mPrefs;					// the preferences instance
	CompassKeyboardView		ckv;					// the current layout view, either @ckv or @ckvVertical
	String				currentLayout;
	StringBuilder		sb;
	boolean				lastInPortrait;
	DisplayMetrics			lastMetrics = new DisplayMetrics();
	boolean				forcePortrait;				// use the portrait layout even for horizontal screens
	ExtractedTextRequest		etreq = new ExtractedTextRequest();
	int				selectionStart = -1, selectionEnd = -1;
	DbHelper db;
	FileHelper fp;
	// send an auto-revoked notification with a title and a message
	void sendNotification(String title, String msg) {
	}
	public void skipLayout(XmlPullParser parser) throws XmlPullParserException, IOException {
		while ((parser.getEventType() != XmlPullParser.END_TAG) || !parser.getName().contentEquals("Layout"))
			parser.nextTag();
		parser.nextTag();
	}
	// Read a layout from a parser
	String updateLayout(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		Log.i("ZAIC_TESTMSG","updateLayout invoked");
		//KbdModel layout=(KbdModel)ois.readObject();
		KbdModel layout;
		try {
			layout = (KbdModel) ois.readObject();
			ois.close();
		} catch(FileNotFoundException e) {
			layout = KeySettingActivity.init(3, 5);
		} catch (Exception e) {
			e.printStackTrace();
			layout = KeySettingActivity.init(3, 5);
		}

		ckv.readLayout(layout);
		return layout.kbdName;
	}
	public String updateLayout(String filename){
		String result = "same";
		String err = null;
		if (filename.contentEquals(currentLayout))
			return result;
		try {
			FileInputStream fis;
			if (filename.contentEquals("default")) {
				//result = updateLayout(new ObjectInputStream(getResources().openRawResource(R.raw.latin)));
				try {
					fis = openFileInput("userKbdModel");
					ObjectInputStream ois = new ObjectInputStream(fis);
					ckv.readLayout((KbdModel) ois.readObject());
						ois.close();
					} catch(FileNotFoundException e) {
						ckv.readLayout(KeySettingActivity.init(3, 5));
					} catch (Exception e) {
						e.printStackTrace();
						ckv.readLayout(KeySettingActivity.init(3, 5));
					}
			}
			else {
				result = updateLayout(new ObjectInputStream(new FileInputStream(filename)));
			}
		}
		catch (FileNotFoundException e)		{ err = e.getMessage(); }
		catch (IOException e)		{ err = e.getMessage(); }
		catch(ClassNotFoundException e) { err = e.getMessage();}
		if (err == null) {
			currentLayout = filename;	// loaded successfully, we may store it as 'current'
			return result;
		}
		sendNotification("Invalid layout", err);
		// revert to default latin, unless this was the one that has failed
		if (!filename.contentEquals("default")) {
			currentLayout = "";
			return updateLayout("default");
		}
		return "failed";
	}
	public String updateLayout(int i) {
		String s = mPrefs.getString("layout_path_" + String.valueOf(i), "");
		if (s.length() > 0)
			return updateLayout(s);
		return "failed";
	}
	@Override public AbstractInputMethodImpl onCreateInputMethodInterface() {
		Log.d(TAG, "onCreateInputMethodInterface;");
		etreq.hintMaxChars = etreq.hintMaxLines = 0;
		mPrefs = getSharedPreferences(SHARED_PREFS_NAME, 0);
		ckv = new CompassKeyboardView(this);
		ckv.setOnKeyboardActionListener(this);
		forcePortrait = mPrefs.getBoolean("portrait_only", false);
		lastMetrics.setTo(getResources().getDisplayMetrics());
		lastInPortrait = forcePortrait || (lastMetrics.widthPixels <= lastMetrics.heightPixels);
		currentLayout = "";			// enforce reloading layout
		db=new DbHelper(this);
		fp=new FileHelper(this);
		Log.i("ZAIC","Before ReadFile");
		fp.readFile();
		Log.i("ZAIC","After ReadFile");
		db.fileToDB(fp);
		updateLayout(mPrefs.getString("layout", "default"));
		ckv.setVibrateOnKey(getPrefInt("feedback_key", 0));
		ckv.setVibrateOnModifier(getPrefInt("feedback_mod", 0));
		ckv.setVibrateOnCancel(getPrefInt("feedback_cancel", 0));
		ckv.setFeedbackNormal(getPrefInt("feedback_text", 0));
		ckv.setFeedbackPassword(getPrefInt("feedback_password", 0));
		ckv.setLeftMargin(getPrefFloat("margin_left", 0));
		ckv.setRightMargin(getPrefFloat("margin_right", 0));
		ckv.setBottomMargin(getPrefFloat("margin_bottom", 0));
		ckv.setMaxKeySize(getPrefFloat("max_keysize", 12));
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		return super.onCreateInputMethodInterface();
	}
	// Select the layout view appropriate for the screen direction, if there is more than one
	@Override public View onCreateInputView() {
		DisplayMetrics metrics = new DisplayMetrics();
		metrics.setTo(getResources().getDisplayMetrics());
		Log.v(TAG, "onCreateInputView; w=" + String.valueOf(metrics.widthPixels) + ", h=" + String.valueOf(metrics.heightPixels) + ", forceP=" + String.valueOf(forcePortrait));
		Log.v(TAG, "onCreateInputView; last w=" + String.valueOf(lastMetrics.widthPixels) + ", h=" + String.valueOf(lastMetrics.heightPixels) + ", forceP=" + String.valueOf(forcePortrait));
		if ((metrics.widthPixels != lastMetrics.widthPixels) || (metrics.heightPixels != lastMetrics.heightPixels)) {
			lastMetrics.setTo(metrics);
			boolean inPortrait = forcePortrait || (lastMetrics.widthPixels <= lastMetrics.heightPixels);
			Log.v(TAG, "onCreateInputView; metrics changed, inPortrait=" + String.valueOf(inPortrait) + ", lastInPortrait=" + String.valueOf(lastInPortrait));
			if (inPortrait != lastInPortrait) {
				lastInPortrait = inPortrait;
				String s = currentLayout;
				currentLayout = "";			// enforce reloading layout
				updateLayout(s);
			}
			else {
				ckv.calculateSizesForMetrics(lastMetrics);	// don't reload, only resize
			}
		}
		ViewParent p = ckv.getParent();
		if ((p != null) && (p instanceof ViewGroup))
			((ViewGroup)p).removeView(ckv);
		return ckv;
	} 
	@Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
		//Log.d(TAG, "onStartInputView;");
		mCandidateView.clear();
		sb=null;
		super.onStartInputView(attribute, restarting);
		updateLayout("");
		if (ckv != null) {
			ckv.setInputType(attribute.inputType);
		}
	}
	@Override public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting); 
		if (ckv != null) {
			ckv.setInputType(attribute.inputType);
		}
	}
	@Override public boolean onEvaluateFullscreenMode() {
		return false; // never require fullscreen
	}
	private void sendModifiers(InputConnection ic, int action) {
		if (ckv == null)
			return;
	}
	// Process a generated keycode
	public void onKey(int primaryCode, int[] keyCodes) {
		sb=null;
		mCandidateView.clear();
		getCurrentInputConnection().finishComposingText();
		sendDownUpKeyEvents(primaryCode);
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		sb=null;
		getCurrentInputConnection().finishComposingText();
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				// The InputMethodService already takes care of the back
				// key for us, to dismiss the input method if it is shown.
				// However, our keyboard could be showing a pop-up window
				// that back should dismiss, so we first allow it to do that.
				return super.onKeyDown(keyCode,event);

			case KeyEvent.KEYCODE_DEL:

				// Special handling of the delete key: if we currently are
				// composing text for the user, we want to modify that instead
				// of let the application to the delete itself.
				if (mComposing.length() > 0) {
					onKey(Keyboard.KEYCODE_DELETE, null);
					return true;
				}
				break;

			case KeyEvent.KEYCODE_ENTER:
				// Let the underlying text editor always handle these.
				return false;
			default:
				// For all other keys, if we want to do transformations on
				// text being entered with a hard keyboard, we need to process
				// it and do the appropriate action.
                /*
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }*/
		}

		return super.onKeyDown(keyCode, event);
	}
	// Process the generated text
	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		if(text==" "){
			ic.finishComposingText();
			sb=null;
			return;
		}
		sendModifiers(ic, KeyEvent.ACTION_DOWN);
		if(sb==null) sb=new StringBuilder(text);
		else sb.append(text);
		ArrayList<String> suggList=db.search(sb.toString());
		suggList.add(0,sb.toString());
		mCandidateView.setSuggestions(suggList,true,true);
		ic.setComposingText(sb,sb.length());
		//sendKeyChar(text.charAt(0));
	} 
	// Process a command
	public void execCmd(String cmd) {
		InputConnection ic = getCurrentInputConnection();
		if (cmd.equals("selectStart")) {
			selectionStart = ic.getExtractedText(etreq, 0).selectionStart;
			if ((selectionStart >= 0) && (selectionEnd >= 0)) {
				ic.setSelection(selectionStart, selectionEnd);
				selectionStart = selectionEnd = -1;
			}
		}
		else if (cmd.equals("selectEnd")) {
			selectionEnd = ic.getExtractedText(etreq, 0).selectionEnd;
			if ((selectionStart >= 0) && (selectionEnd >= 0)) {
				ic.setSelection(selectionStart, selectionEnd);
				selectionStart = selectionEnd = -1;
			}
		}
		else if (cmd.equals("selectAll"))
			ic.performContextMenuAction(id.selectAll);
		else if (cmd.equals("copy"))
			ic.performContextMenuAction(id.copy);
		else if (cmd.equals("cut"))
			ic.performContextMenuAction(id.cut);
		else if (cmd.equals("paste"))
			ic.performContextMenuAction(id.paste);
		else if (cmd.equals("switchIM"))
			ic.performContextMenuAction(id.switchInputMethod);
		else
			Log.w(TAG, "Unknown cmd '" + cmd + "'");
	}
	public void pickDefaultCandidate() {
		pickSuggestionManually(0);
	}
	public void swipeRight() {
	}
	public void swipeLeft() {
	}
	// Hide the view
	public void swipeDown() {
		requestHideSelf(0);
	}
	public void swipeUp() {
	}
	public void onPress(int primaryCode) {
	}
	public void onRelease(int primaryCode) {
	} 
	String getPrefString(String key, String def) {
		String s = "";
		if (lastInPortrait) {
			s = mPrefs.getString("portrait_" + key, "");
			if (s.contentEquals(""))
				s = mPrefs.getString("landscape_" + key, "");
		}
		else {
			s = mPrefs.getString("landscape_" + key, "");
			if (s.contentEquals(""))
				s = mPrefs.getString("portrait_" + key, "");
		}
		if (s.contentEquals(""))
			s = mPrefs.getString(key, "");
		return s.contentEquals("") ? def : s;
	}
	int getPrefInt(String key, int def) {
		String s = getPrefString(key, "");
		try {
			return s.contentEquals("") ? def : Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			Log.w(TAG, "Invalid value for integer preference; key='" + key + "', value='" + s +"'");
		}
		catch (ClassCastException e) {
			Log.w(TAG, "Found non-string int preference; key='" + key + "', err='" + e.getMessage() + "'");
		}
		return def;
	}
	float getPrefFloat(String key, float def) {
		String s = getPrefString(key, "");
		try {
			return s.contentEquals("") ? def : Float.parseFloat(s);
		}
		catch (NumberFormatException e) {
			Log.w(TAG, "Invalid value for float preference; key='" + key + "', value='" + s +"'");
		}
		catch (ClassCastException e) {
			Log.w(TAG, "Found non-string float preference; key='" + key + "', err='" + e.getMessage() + "'");
		}
		return def;
	}
	// Handle one change in the preferences
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		//Log.d(TAG, "Changing pref "+key);
		if (key.contentEquals("feedback_key"))
			ckv.setVibrateOnKey(getPrefInt("feedback_key", 0));
		else if (key.contentEquals("feedback_mod"))
			ckv.setVibrateOnModifier(getPrefInt("feedback_mod", 0));
		else if (key.contentEquals("feedback_cancel"))
			ckv.setVibrateOnCancel(getPrefInt("feedback_cancel", 0));
		else if (key.contentEquals("feedback_text"))
			ckv.setFeedbackNormal(getPrefInt("feedback_text", 0));
		else if (key.contentEquals("feedback_password"))
			ckv.setFeedbackPassword(getPrefInt("feedback_password", 0));
		else if (key.endsWith("margin_left")) {
			ckv.setLeftMargin(getPrefFloat("margin_left", 0));
			getWindow().dismiss();
		} else if (key.endsWith("margin_right")) {
			ckv.setRightMargin(getPrefFloat("margin_right", 0));
			getWindow().dismiss();
		} else if (key.endsWith("margin_bottom")) {
			ckv.setBottomMargin(getPrefFloat("margin_bottom", 0));
			getWindow().dismiss();
		} else if (key.endsWith("max_keysize")) {
			ckv.setMaxKeySize(getPrefFloat("max_keysize", 12));
			getWindow().dismiss();
		} else if (key.contentEquals("layout"))
			updateLayout(mPrefs.getString("layout", "@latin"));
		else if (key.startsWith("layout_path_")) {
			int i = Integer.parseInt(key.substring(12));
			// ...
		} else if (key.contentEquals("portrait_only"))
			forcePortrait = mPrefs.getBoolean("portrait_only", false);
	}

	private boolean mCompletionOn;
	private CandidateView mCandidateView;
	private CompletionInfo[] mCompletions;
	StringBuilder mComposing = new StringBuilder();
	private boolean mPredictionOn;
	private List<String> mSuggestions;
	private SpellCheckerSession mScs;
	@Override public View onCreateCandidatesView() {
		Log.d("CAUADD","onCreateCandidatesView Invoked");
		mCandidateView = new CandidateView(this);
		mCandidateView.setService(this);
		mCandidateView.setSuggestions(new ArrayList<String>(Arrays.asList("ASDF","GHJK","LMNO")),true,true);
		setCandidatesViewShown(true);
		//ckv.addView(mCandidateView,0);
		return mCandidateView;
	}
	public void pickSuggestionManually(int index) {
		if (mCompletionOn && mCompletions != null && index >= 0
				&& index < mCompletions.length) {
			CompletionInfo ci = mCompletions[index];
			getCurrentInputConnection().commitCompletion(ci);
			if (mCandidateView != null) {
				mCandidateView.clear();
			}
		} else if (mComposing.length() > 0) {
			if (mPredictionOn && mSuggestions != null && index >= 0) {
				mComposing.replace(0, mComposing.length(), mSuggestions.get(index));
			}
			commitTyped(getCurrentInputConnection());
		}
	}
	private void commitTyped(InputConnection inputConnection) {
		if (mComposing.length() > 0) {
			inputConnection.commitText(mComposing, mComposing.length());
			mComposing.setLength(0);
			updateCandidates();
		}
	}
	private void updateCandidates() {
		if (!mCompletionOn) {
			if (mComposing.length() > 0) {
				ArrayList<String> list = new ArrayList<String>();
				//list.add(mComposing.toString());
				Log.d("SoftKeyboard", "REQUESTING: " + mComposing.toString());
				mScs.getSentenceSuggestions(new TextInfo[] {new TextInfo(mComposing.toString())}, 5);
			} else {
				setSuggestions(null, false, false);
			}
		}
	}
	public void setSuggestions(List<String> suggestions, boolean completions,
							   boolean typedWordValid) {
		if (suggestions != null && suggestions.size() > 0) {
			setCandidatesViewShown(true);
		} else if (isExtractViewShown()) {
			setCandidatesViewShown(true);
		}
		mSuggestions = suggestions;
		if (mCandidateView != null) {
			mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
		}
	}
}
// vim: set ai si sw=8 ts=8 noet:
