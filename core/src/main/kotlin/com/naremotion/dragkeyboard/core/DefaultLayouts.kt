package com.naremotion.dragkeyboard.core

/** 처음 설치했을 때 제공하는 자판. 칸은 [Direction] 순서(왼쪽 위부터 3x3)로 적는다. */
object DefaultLayouts {
    const val CLASSIC_ID = "default-classic"
    const val ENGLISH_ID = "default-english"
    const val NUMBER_PAD_ID = "builtin-number-pad"
    const val PHONE_PAD_ID = "builtin-phone-pad"

    fun all(): List<KeyboardLayout> = listOf(classic(), english())

    /** 2018년 원작의 기본 자판: 한글·영문·숫자·기호가 3x5 한 장에 모두 들어 있다. */
    fun classic() = layout(
        CLASSIC_ID, "기본 (한/영)", Language.KOREAN, 5,
        key("`", "ㄲ", "", "ㅎ", "ㅇ", "ㅋ", "🌐", "ㄱ", "⌨▸"),
        key("{", "ㄸ", "}", "ㄹ", "ㄴ", "ㅌ", "=", "ㄷ", "0"),
        key("1", "2", "3", "4", "5", "6", "7", "8", "9"),
        key("ㅖ", "ㅛ", "ㅒ", "ㅕ", "ㅣ", "ㅑ", "ㅖ", "ㅠ", "ㅒ"),
        key("(", ",", ")", "<", "⌫", ">", "[", ".", "]", repeat = true),

        key("+", "ㅆ", "-", "ㅊ", "ㅅ", "ㅉ", "*", "ㅈ", "/"),
        key("'", "ㅃ", "\"", "ㅍ", "ㅁ", "ㅍ", ";", "ㅂ", ":"),
        key("!", "@", "#", "$", "%", "^", "&", "*", "?"),
        key("ㅔ", "ㅗ", "ㅐ", "ㅓ", "ㅡ", "ㅏ", "ㅔ", "ㅜ", "ㅐ"),
        key("s", "t", "u", "v", "⏎", "w", "x", "y", "z"),

        key("A", "B", "C", "D", "E", "F", "G", "H", "I"),
        key("J", "K", "L", "M", "N", "O", "P", "Q", "R"),
        key("S", "T", "U", "V", "␣", "W", "X", "Y", "Z"),
        key("a", "b", "c", "d", "e", "f", "g", "h", "i"),
        key("j", "k", "l", "m", "n", "o", "p", "q", "r"),
    )

    /** QWERTY 배열을 3x3 묶음으로 옮긴 영문 자판: 글자가 있던 쪽으로 드래그한다. */
    fun english() = layout(
        ENGLISH_ID, "English", Language.ENGLISH, 5,
        key("q", "w", "e", "a", "s", "d", "z", "x", "c"),
        key("r", "t", "y", "f", "g", "h", "v", "b", "n"),
        key("u", "i", "o", "j", "k", "l", "m", ",", "."),
        key("p", "'", "\"", ";", ".", "!", "-", "?", ":"),
        key("(", ",", ")", "<", "⌫", ">", "[", ".", "]", repeat = true),

        key("Q", "W", "E", "A", "S", "D", "Z", "X", "C"),
        key("R", "T", "Y", "F", "G", "H", "V", "B", "N"),
        key("U", "I", "O", "J", "K", "L", "M", "<", ">"),
        key("P", "#", "$", "%", "&", "*", "+", "=", "_"),
        key("⇥", "", "", "◀", "⏎", "▶", "", "", ""),

        key("1", "2", "3", "4", "5", "6", "7", "8", "9"),
        key("~", "^", "`", "{", "0", "}", "[", "\\", "]"),
        key("", "", "", "◀", "␣", "▶", "", "", "", repeat = true),
        key("<", "\"", ">", "(", ",", ")", "|", ".", "/"),
        key("", "", "", "◂⌨", "⌨▸", "", "", "🌐", ""),
    )

    /**
     * 숫자 입력칸용 3x4 키패드 + 오른쪽 기능 열. 사용자 자판 목록에는 들어가지 않는다.
     * 왼쪽 아래 키는 탭하면 -, 밀면 + / : * 이 나와 숫자·날짜·시간 입력을 함께 처리한다.
     */
    fun numberPad() = layout(
        NUMBER_PAD_ID, "123", Language.ENGLISH, 4,
        digit("1"), digit("2"), digit("3"), key("", "", "", "", "⌫", "", "", "", "", repeat = true),
        digit("4"), digit("5"), digit("6"), key("", "", "", "◀", "␣", "▶", "", "", ""),
        digit("7"), digit("8"), digit("9"), key("", "", "", "", "⏎", "", "", "", ""),
        key("", "+", "", "/", "-", ":", "", "*", ""), digit("0"), key("", ",", "", "", ".", "", "", "", ""),
        key("", "", "", "", "🌐", "", "", "", ""),
    )

    /** 전화번호 입력칸용 키패드. * 키를 밀면 + - ( ), # 키를 밀면 , ; (일시정지·대기)가 나온다. */
    fun phonePad() = layout(
        PHONE_PAD_ID, "☎", Language.ENGLISH, 4,
        digit("1"), digit("2"), digit("3"), key("", "", "", "", "⌫", "", "", "", "", repeat = true),
        digit("4"), digit("5"), digit("6"), key("", "", "", "◀", "␣", "▶", "", "", ""),
        digit("7"), digit("8"), digit("9"), key("", "", "", "", "⏎", "", "", "", ""),
        key("", "+", "", "(", "*", ")", "", "-", ""), key("", "+", "", "", "0", "", "", "", ""),
        key("", ",", "", "", "#", "", "", ";", ""), key("", "", "", "", "🌐", "", "", "", ""),
    )

    private fun digit(d: String) = key("", "", "", "", d, "", "", "", "")

    private fun layout(id: String, name: String, language: Language, columns: Int, vararg keys: KeySpec) =
        KeyboardLayout(id, name, language, keys.size / columns, columns, keys.toList())

    private fun key(vararg cells: String, repeat: Boolean = false): KeySpec {
        require(cells.size == 9)
        return KeySpec(cells.map(::parseCell), repeat)
    }

    private fun parseCell(cell: String): KeyAction? = when (cell) {
        "" -> null
        "␣" -> KeyAction.Text(" ")
        "🌐" -> KeyAction.SwitchInputMethod()
        else -> SpecialKey.entries.firstOrNull { it.symbol == cell }?.let { KeyAction.Special(it) }
            ?: LayoutTarget.entries.firstOrNull { it.symbol == cell }?.let { KeyAction.SwitchLayout(it) }
            ?: KeyAction.Text(cell)
    }
}
