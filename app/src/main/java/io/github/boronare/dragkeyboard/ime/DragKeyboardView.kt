package io.github.boronare.dragkeyboard.ime

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import io.github.boronare.dragkeyboard.core.Direction
import io.github.boronare.dragkeyboard.core.DirectionDetector
import io.github.boronare.dragkeyboard.core.KeyAction
import io.github.boronare.dragkeyboard.core.KeyboardLayout
import io.github.boronare.dragkeyboard.core.SpecialKey
import io.github.boronare.dragkeyboard.data.KeyboardPrefs
import io.github.boronare.dragkeyboard.data.Vibration
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 자판을 그리고 터치를 방향 입력으로 바꾸는 뷰.
 * 각 키에는 9개 방향의 글자가 3x3으로 표시되고, 누른 채 드래그하면 그 방향의 글자가 입력된다.
 *
 * 좌우 여백은 가장자리 키를 바깥으로 드래그할 공간이다. 키에서 시작한 드래그는 여백까지 밀어도
 * 그 키의 입력이고, 여백에서 시작한 터치만 자판에 지정된 여백 원터치 동작을 실행한다.
 */
@SuppressLint("ViewConstructor")
class DragKeyboardView(context: Context) : View(context) {
    var onAction: ((KeyAction) -> Unit)? = null

    /** 입력창에 맞춘 엔터 표시(🔍, ➤ …). null이면 기본 ⏎. 표시 값을 직접 정한 엔터 키는 그대로 둔다. */
    var enterLabel: String? = null
        set(value) {
            field = value
            invalidate()
        }

    private var layout: KeyboardLayout? = null
    private var prefs = KeyboardPrefs()

    private val mmPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, resources.displayMetrics)
    private val dpPx = resources.displayMetrics.density
    private val gap = 4 * dpPx
    private val radius = 6 * dpPx

    private val keyRects = mutableListOf<RectF>()
    private var leftEdgeRect: RectF? = null
    private var rightEdgeRect: RectF? = null
    private var gridLeft = 0f
    private var gridRight = 0f
    private var gridTop = 0f
    private var cellWidth = 1f
    private var cellHeight = 1f

    /** 터치가 시작된 곳: 키 번호이거나 [LEFT_EDGE], [RIGHT_EDGE]. */
    private inner class Touch(val target: Int, val downX: Float, val downY: Float) : Runnable {
        var direction = Direction.TAP
        var repeated = false
        val isEdge get() = target < 0

        /** 손을 뗐을 때 실행할 동작. 여백 버튼은 방향과 상관없이 하나뿐이다. */
        fun action(): KeyAction? {
            val l = layout ?: return null
            return when (target) {
                LEFT_EDGE -> l.leftEdge
                RIGHT_EDGE -> l.rightEdge
                else -> l.keys.getOrNull(target)?.get(direction)
            }
        }

        fun repeats(): Boolean {
            val l = layout ?: return false
            return if (isEdge) {
                (action() as? KeyAction.Special)?.key in REPEATABLE_KEYS
            } else {
                val key = l.keys.getOrNull(target) ?: return false
                key.repeat && key[Direction.TAP] != null
            }
        }

        // 길게 누르면 가운데 동작을 반복한다
        override fun run() {
            if (direction != Direction.TAP) return
            val action = action() ?: return
            repeated = true
            onAction?.invoke(action)
            postDelayed(this, REPEAT_INTERVAL_MS)
        }
    }

    private val touches = HashMap<Int, Touch>()

    private val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    private val backgroundColor = if (night) 0xFF1E1F22.toInt() else 0xFFD5D8DE.toInt()
    private val keyColor = if (night) 0xFF3A3B3F.toInt() else 0xFFFFFFFF.toInt()
    private val edgeColor = if (night) 0xFF2B2C30.toInt() else 0xFFE9EBEF.toInt()
    private val pressedColor = if (night) 0xFF2F4F80.toInt() else 0xFFB9CCEE.toInt()
    private val primaryText = if (night) 0xFFF1F1F4.toInt() else 0xFF1B1B1F.toInt()
    private val secondaryText = if (night) 0xFF9AA0A6.toInt() else 0xFF6B7280.toInt()

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    fun bind(layout: KeyboardLayout, prefs: KeyboardPrefs) {
        cancelTouches()
        this.layout = layout
        this.prefs = prefs
        requestLayout()
        computeKeyRects()
        invalidate()
    }

    private val margins
        get() = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            prefs.landscapeMargins
        } else {
            prefs.portraitMargins
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val rows = layout?.rows ?: 1
        val wanted = rows * prefs.keyHeightMm * mmPx + margins.bottomMm * mmPx + gap
        // 가로 모드에서 화면을 다 덮지 않도록 높이를 제한한다
        val height = min(wanted, resources.displayMetrics.heightPixels * MAX_HEIGHT_RATIO)
        setMeasuredDimension(width, height.roundToInt())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        computeKeyRects()
    }

    private fun computeKeyRects() {
        keyRects.clear()
        leftEdgeRect = null
        rightEdgeRect = null
        val l = layout ?: return
        if (width == 0 || height == 0) return
        gridLeft = margins.leftMm * mmPx
        gridRight = width - margins.rightMm * mmPx
        gridTop = gap / 2
        val gridBottom = height - margins.bottomMm * mmPx - gap / 2
        cellWidth = ((gridRight - gridLeft) / l.columns).coerceAtLeast(1f)
        cellHeight = ((gridBottom - gridTop) / l.rows).coerceAtLeast(1f)
        for (r in 0 until l.rows) {
            for (c in 0 until l.columns) {
                keyRects += RectF(
                    gridLeft + c * cellWidth + gap / 2,
                    gridTop + r * cellHeight + gap / 2,
                    gridLeft + (c + 1) * cellWidth - gap / 2,
                    gridTop + (r + 1) * cellHeight - gap / 2,
                )
            }
        }
        // 여백이 손가락으로 누를 만큼 넓을 때만 원터치 버튼을 둔다
        val minEdge = MIN_EDGE_BUTTON_MM * mmPx
        if (l.leftEdge != null && gridLeft >= minEdge) {
            leftEdgeRect = RectF(gap / 2, gridTop + gap / 2, gridLeft - gap / 2, gridBottom - gap / 2)
        }
        if (l.rightEdge != null && width - gridRight >= minEdge) {
            rightEdgeRect = RectF(gridRight + gap / 2, gridTop + gap / 2, width - gap / 2, gridBottom - gap / 2)
        }
    }

    /** 터치가 시작된 대상. 빈 여백이나 키 사이 틈은 가장 가까운 키로 처리해 입력이 빠지지 않게 한다. */
    private fun targetAt(x: Float, y: Float): Int? {
        val l = layout ?: return null
        if (keyRects.isEmpty()) return null
        if (leftEdgeRect != null && x < gridLeft) return LEFT_EDGE
        if (rightEdgeRect != null && x >= gridRight) return RIGHT_EDGE
        val c = floor((x - gridLeft) / cellWidth).toInt().coerceIn(0, l.columns - 1)
        val r = floor((y - gridTop) / cellHeight).toInt().coerceIn(0, l.rows - 1)
        return r * l.columns + c
    }

    private fun isPressed(target: Int) = touches.values.any { it.target == target }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        val l = layout ?: return
        leftEdgeRect?.let { drawEdge(canvas, it, l.leftEdge, isPressed(LEFT_EDGE)) }
        rightEdgeRect?.let { drawEdge(canvas, it, l.rightEdge, isPressed(RIGHT_EDGE)) }
        keyRects.forEachIndexed { index, rect ->
            val key = l.keys[index]
            val touch = touches.values.firstOrNull { it.target == index }
            keyPaint.color = if (touch != null) pressedColor else keyColor
            canvas.drawRoundRect(rect, radius, radius, keyPaint)

            val selected = touch?.action()
            if (selected != null) {
                drawLabel(canvas, labelOf(selected), rect.centerX(), rect.centerY(),
                    rect.width() * 0.9f, rect.height() * 0.6f, primaryText, bold = true)
                return@forEachIndexed
            }
            val subW = rect.width() / 3
            val subH = rect.height() / 3
            for (d in Direction.entries) {
                val action = key[d] ?: continue
                val cx = rect.left + subW * (d.gridCol + 0.5f)
                val cy = rect.top + subH * (d.gridRow + 0.5f)
                if (d == Direction.TAP) {
                    drawLabel(canvas, labelOf(action), cx, cy, subW * 1.2f, subH * 1.05f, primaryText, bold = true)
                } else {
                    drawLabel(canvas, labelOf(action), cx, cy, subW * 0.95f, subH * 0.7f, secondaryText, bold = false)
                }
            }
        }
    }

    private fun drawEdge(canvas: Canvas, rect: RectF, action: KeyAction?, pressed: Boolean) {
        keyPaint.color = if (pressed) pressedColor else edgeColor
        canvas.drawRoundRect(rect, radius, radius, keyPaint)
        if (action != null) {
            drawLabel(canvas, labelOf(action), rect.centerX(), rect.centerY(),
                rect.width() * 0.85f, min(rect.width() * 0.7f, cellHeight * 0.35f), primaryText, bold = true)
        }
    }

    private fun labelOf(action: KeyAction): String {
        val enter = enterLabel
        return if (enter != null && action is KeyAction.Special && action.key == SpecialKey.ENTER && action.label.isNullOrEmpty()) {
            enter
        } else {
            action.displayLabel
        }
    }

    private fun drawLabel(
        canvas: Canvas, text: String, cx: Float, cy: Float,
        maxWidth: Float, size: Float, color: Int, bold: Boolean,
    ) {
        textPaint.color = color
        textPaint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        textPaint.textSize = size
        val w = textPaint.measureText(text)
        if (w > maxWidth) textPaint.textSize = size * maxWidth / w
        val baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(text, cx, baseline, textPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val i = event.actionIndex
                val target = targetAt(event.getX(i), event.getY(i)) ?: return true
                val touch = Touch(target, event.getX(i), event.getY(i))
                touches[event.getPointerId(i)] = touch
                if (touch.repeats()) postDelayed(touch, LONG_PRESS_MS)
                feedback()
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val touch = touches[event.getPointerId(i)] ?: continue
                    updateDirection(touch, event.getX(i), event.getY(i))
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val i = event.actionIndex
                val touch = touches.remove(event.getPointerId(i)) ?: return true
                removeCallbacks(touch)
                updateDirection(touch, event.getX(i), event.getY(i))
                if (!touch.repeated) touch.action()?.let { onAction?.invoke(it) }
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> cancelTouches()
        }
        return true
    }

    private fun updateDirection(touch: Touch, x: Float, y: Float) {
        if (touch.isEdge) return
        val thresholdY = prefs.sensitivityYMm * mmPx
        var dy = y - touch.downY
        // 일부 기기는 키보드 창 위로 나간 터치의 y를 0에 묶어 버린다 (원작의 "위쪽 인식 불량").
        // 위 경계에 닿았으면 대각선까지 인식될 만큼 위로 그은 것으로 본다.
        if (y <= 0f) dy = min(dy, -thresholdY * prefs.diagonalScale * TOP_EDGE_BOOST)
        val direction = DirectionDetector.detect(
            x - touch.downX, dy,
            prefs.sensitivityXMm * mmPx, thresholdY, prefs.diagonalScale,
        )
        if (direction == touch.direction) return
        touch.direction = direction
        if (direction != Direction.TAP) removeCallbacks(touch)
        invalidate()
    }

    private fun cancelTouches() {
        touches.values.forEach { removeCallbacks(it) }
        touches.clear()
        invalidate()
    }

    private fun feedback() {
        when (prefs.vibration) {
            Vibration.OFF -> Unit
            Vibration.LIGHT -> performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            Vibration.STRONG -> vibrator?.vibrate(VibrationEffect.createOneShot(STRONG_VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onDetachedFromWindow() {
        cancelTouches()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val LEFT_EDGE = -1
        const val RIGHT_EDGE = -2
        const val LONG_PRESS_MS = 400L
        const val REPEAT_INTERVAL_MS = 60L
        const val STRONG_VIBRATION_MS = 30L
        const val MAX_HEIGHT_RATIO = 0.55f
        const val MIN_EDGE_BUTTON_MM = 3f
        const val TOP_EDGE_BOOST = 1.05f
        val REPEATABLE_KEYS = setOf(
            SpecialKey.BACKSPACE, SpecialKey.FORWARD_DELETE, SpecialKey.CURSOR_LEFT, SpecialKey.CURSOR_RIGHT,
        )
    }
}
