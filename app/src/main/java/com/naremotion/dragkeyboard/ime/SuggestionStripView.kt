package com.naremotion.dragkeyboard.ime

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

/** 키보드 위의 단어 추천 줄. 가장 좋은 후보를 가운데에 둔다 (업계 키보드와 같은 배치). */
@SuppressLint("ViewConstructor")
class SuggestionStripView(context: Context) : View(context) {
    var onPick: ((String) -> Unit)? = null

    private var slots: List<String?> = listOf(null, null, null)
    private var pressed = -1

    private val dpPx = resources.displayMetrics.density
    private val night = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES
    private val backgroundColor = if (night) 0xFF1E1F22.toInt() else 0xFFD5D8DE.toInt()
    private val pressedColor = if (night) 0xFF2F4F80.toInt() else 0xFFB9CCEE.toInt()
    private val textColor = if (night) 0xFFF1F1F4.toInt() else 0xFF1B1B1F.toInt()
    private val dividerColor = if (night) 0xFF3A3B3F.toInt() else 0xFFBFC3CA.toInt()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    /** [suggestions]는 좋은 순서. 1등은 가운데, 2등은 왼쪽, 3등은 오른쪽에 놓는다. */
    fun setSuggestions(suggestions: List<String>) {
        val new = listOf(suggestions.getOrNull(1), suggestions.getOrNull(0), suggestions.getOrNull(2))
        if (new == slots) return
        slots = new
        pressed = -1
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (HEIGHT_DP * dpPx).roundToInt())
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
        val slotWidth = width / 3f
        slots.forEachIndexed { i, word ->
            val left = i * slotWidth
            if (i == pressed && word != null) {
                paint.color = pressedColor
                canvas.drawRect(left, 0f, left + slotWidth, height.toFloat(), paint)
            }
            if (i > 0) {
                paint.color = dividerColor
                canvas.drawRect(left - dpPx / 2, height * 0.25f, left + dpPx / 2, height * 0.75f, paint)
            }
            if (word == null) return@forEachIndexed
            paint.color = textColor
            paint.typeface = if (i == 1) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            paint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, TEXT_SP, resources.displayMetrics)
            val w = paint.measureText(word)
            val max = slotWidth - 12 * dpPx
            if (w > max) paint.textSize *= max / w
            val baseline = height / 2f - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(word, left + slotWidth / 2, baseline, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val slot = (event.x / (width / 3f)).toInt().coerceIn(0, 2)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressed = slot
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                val word = slots[slot]
                if (slot == pressed && word != null) onPick?.invoke(word)
                pressed = -1
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                pressed = -1
                invalidate()
            }
        }
        return true
    }

    private companion object {
        const val HEIGHT_DP = 40f
        const val TEXT_SP = 16f
    }
}
