package io.github.boronare.dragkeyboard.core

/**
 * 단어 검색용 정규화. 한글은 입력 순서대로의 자모열로 풀어서, 조합 중인 글자도 접두어로 맞출 수 있게 한다.
 * 예: "한"(ㅎㅏㄴ)은 "하나"(ㅎㅏㄴㅏ)의 접두어다. 받침 ㄴ이 다음 글자의 초성으로 넘어갈 수 있기 때문이다.
 * 겹모음과 겹받침은 입력할 때처럼 둘로 나눈다 (과 → ㄱㅗㅏ, 닭 → ㄷㅏㄹㄱ). 라틴 문자는 소문자로 바꾼다.
 */
object Jamo {
    private const val CHO = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
    private val JUNG = listOf(
        "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅗㅏ", "ㅗㅐ",
        "ㅗㅣ", "ㅛ", "ㅜ", "ㅜㅓ", "ㅜㅔ", "ㅜㅣ", "ㅠ", "ㅡ", "ㅡㅣ", "ㅣ",
    )
    private val JONG = listOf(
        "", "ㄱ", "ㄲ", "ㄱㅅ", "ㄴ", "ㄴㅈ", "ㄴㅎ", "ㄷ", "ㄹ", "ㄹㄱ", "ㄹㅁ", "ㄹㅂ", "ㄹㅅ", "ㄹㅌ",
        "ㄹㅍ", "ㄹㅎ", "ㅁ", "ㅂ", "ㅂㅅ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ",
    )
    private val COMPAT = mapOf(
        'ㅘ' to "ㅗㅏ", 'ㅙ' to "ㅗㅐ", 'ㅚ' to "ㅗㅣ", 'ㅝ' to "ㅜㅓ", 'ㅞ' to "ㅜㅔ", 'ㅟ' to "ㅜㅣ", 'ㅢ' to "ㅡㅣ",
        'ㄳ' to "ㄱㅅ", 'ㄵ' to "ㄴㅈ", 'ㄶ' to "ㄴㅎ", 'ㄺ' to "ㄹㄱ", 'ㄻ' to "ㄹㅁ", 'ㄼ' to "ㄹㅂ",
        'ㄽ' to "ㄹㅅ", 'ㄾ' to "ㄹㅌ", 'ㄿ' to "ㄹㅍ", 'ㅀ' to "ㄹㅎ", 'ㅄ' to "ㅂㅅ",
    )

    fun normalize(text: String): String = buildString(text.length * 3) {
        for (ch in text) {
            when {
                ch in '가'..'힣' -> {
                    val s = ch - '가'
                    append(CHO[s / (21 * 28)])
                    append(JUNG[s / 28 % 21])
                    append(JONG[s % 28])
                }
                ch in COMPAT -> append(COMPAT.getValue(ch))
                else -> append(ch.lowercaseChar())
            }
        }
    }

    fun isHangul(ch: Char): Boolean = ch in '가'..'힣' || ch in 'ㄱ'..'ㅣ'
}
