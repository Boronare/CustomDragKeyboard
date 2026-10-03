package com.naremotion.dragkeyboard.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class SpecialKey(val symbol: String) {
    BACKSPACE("⌫"),
    ENTER("⏎"),
    TAB("⇥"),
    CURSOR_LEFT("◀"),
    CURSOR_RIGHT("▶"),
    FORWARD_DELETE("⌦"),
}

enum class LayoutTarget(val symbol: String) {
    NEXT("⌨▸"),
    PREVIOUS("◂⌨"),
}

/** 한 방향에 지정되는 동작. [label]이 비어 있으면 동작에 맞는 기본 표시값을 쓴다. */
@Serializable
sealed interface KeyAction {
    val label: String?
    val defaultLabel: String

    val displayLabel: String
        get() = label?.takeIf { it.isNotEmpty() } ?: defaultLabel

    /** 문자 입력. 한국어 자판에서는 자모가 음절로 조합된다. */
    @Serializable
    @SerialName("text")
    data class Text(val text: String, override val label: String? = null) : KeyAction {
        init {
            require(text.isNotEmpty()) { "text must not be empty" }
        }

        override val defaultLabel: String
            get() = when (text) {
                " " -> "␣"
                "\n" -> "⏎"
                "\t" -> "⇥"
                else -> text
            }
    }

    @Serializable
    @SerialName("special")
    data class Special(val key: SpecialKey, override val label: String? = null) : KeyAction {
        override val defaultLabel: String get() = key.symbol
    }

    /** Android KeyEvent 키코드를 그대로 보낸다. */
    @Serializable
    @SerialName("keycode")
    data class KeyCode(val code: Int, override val label: String? = null) : KeyAction {
        override val defaultLabel: String get() = "#$code"
    }

    @Serializable
    @SerialName("layout")
    data class SwitchLayout(val target: LayoutTarget, override val label: String? = null) : KeyAction {
        override val defaultLabel: String get() = target.symbol
    }

    /** 시스템의 다른 입력기로 전환한다. */
    @Serializable
    @SerialName("ime")
    data class SwitchInputMethod(override val label: String? = null) : KeyAction {
        override val defaultLabel: String get() = "🌐"
    }
}
