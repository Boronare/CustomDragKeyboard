package io.github.boronare.dragkeyboard.data

import android.content.Context

enum class Vibration { OFF, LIGHT, STRONG }

/** 키보드 좌우·하단 여백 (mm). 모서리 키를 누르기 편하게 한다. */
data class Margins(val leftMm: Float = 0f, val rightMm: Float = 0f, val bottomMm: Float = 0f)

data class KeyboardPrefs(
    val vibration: Vibration = Vibration.LIGHT,
    /** 이 거리(mm) 이상 움직여야 드래그로 인식한다. 작을수록 예민하다. */
    val sensitivityXMm: Float = 3f,
    val sensitivityYMm: Float = 3f,
    val keyHeightMm: Float = 11f,
    val portraitMargins: Margins = Margins(),
    val landscapeMargins: Margins = Margins(),
)

class PrefsStore(context: Context) {
    private val sp = context.applicationContext.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    fun load(): KeyboardPrefs {
        val d = KeyboardPrefs()
        return KeyboardPrefs(
            vibration = sp.getString(VIBRATION, null)
                ?.let { runCatching { Vibration.valueOf(it) }.getOrNull() } ?: d.vibration,
            sensitivityXMm = sp.getFloat(SENSITIVITY_X, d.sensitivityXMm),
            sensitivityYMm = sp.getFloat(SENSITIVITY_Y, d.sensitivityYMm),
            keyHeightMm = sp.getFloat(KEY_HEIGHT, d.keyHeightMm),
            portraitMargins = loadMargins("portrait"),
            landscapeMargins = loadMargins("landscape"),
        )
    }

    fun save(prefs: KeyboardPrefs) {
        sp.edit()
            .putString(VIBRATION, prefs.vibration.name)
            .putFloat(SENSITIVITY_X, prefs.sensitivityXMm)
            .putFloat(SENSITIVITY_Y, prefs.sensitivityYMm)
            .putFloat(KEY_HEIGHT, prefs.keyHeightMm)
            .putMargins("portrait", prefs.portraitMargins)
            .putMargins("landscape", prefs.landscapeMargins)
            .apply()
    }

    /** 키보드에서 마지막으로 사용한 자판. */
    var activeLayoutId: String?
        get() = sp.getString(ACTIVE_LAYOUT, null)
        set(value) = sp.edit().putString(ACTIVE_LAYOUT, value).apply()

    private fun loadMargins(prefix: String) = Margins(
        leftMm = sp.getFloat("$prefix.left", 0f),
        rightMm = sp.getFloat("$prefix.right", 0f),
        bottomMm = sp.getFloat("$prefix.bottom", 0f),
    )

    private fun android.content.SharedPreferences.Editor.putMargins(prefix: String, m: Margins) =
        putFloat("$prefix.left", m.leftMm).putFloat("$prefix.right", m.rightMm).putFloat("$prefix.bottom", m.bottomMm)

    private companion object {
        const val VIBRATION = "vibration"
        const val SENSITIVITY_X = "sensitivity_x"
        const val SENSITIVITY_Y = "sensitivity_y"
        const val KEY_HEIGHT = "key_height"
        const val ACTIVE_LAYOUT = "active_layout"
    }
}
