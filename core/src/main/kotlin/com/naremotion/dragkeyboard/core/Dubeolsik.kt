package com.naremotion.dragkeyboard.core

/**
 * 한글을 두벌식 자판에서 같은 자리에 있는 QWERTY 영문으로 바꾼다 (한 → gks, 닭 → ekfr).
 * 비밀번호 입력칸에서 쓴다. 한글로 생각한 비밀번호를 다른 키보드에서도 똑같이 칠 수 있게 하는 관례다.
 * 된소리와 ㅒ, ㅖ는 Shift 자리(대문자), 겹모음·겹받침은 두 타로 나눈다.
 */
object Dubeolsik {
    private val QWERTY = mapOf(
        'ㅂ' to 'q', 'ㅈ' to 'w', 'ㄷ' to 'e', 'ㄱ' to 'r', 'ㅅ' to 't',
        'ㅛ' to 'y', 'ㅕ' to 'u', 'ㅑ' to 'i', 'ㅐ' to 'o', 'ㅔ' to 'p',
        'ㅁ' to 'a', 'ㄴ' to 's', 'ㅇ' to 'd', 'ㄹ' to 'f', 'ㅎ' to 'g',
        'ㅗ' to 'h', 'ㅓ' to 'j', 'ㅏ' to 'k', 'ㅣ' to 'l',
        'ㅋ' to 'z', 'ㅌ' to 'x', 'ㅊ' to 'c', 'ㅍ' to 'v', 'ㅠ' to 'b', 'ㅜ' to 'n', 'ㅡ' to 'm',
        'ㅃ' to 'Q', 'ㅉ' to 'W', 'ㄸ' to 'E', 'ㄲ' to 'R', 'ㅆ' to 'T', 'ㅒ' to 'O', 'ㅖ' to 'P',
    )

    fun toQwerty(text: String): String = buildString(text.length * 3) {
        for (ch in text) {
            if (Jamo.isHangul(ch)) {
                // 음절과 겹자모를 입력 순서대로의 낱자모로 푼 뒤 자리를 찾는다
                Jamo.normalize(ch.toString()).forEach { append(QWERTY[it] ?: it) }
            } else {
                append(ch)
            }
        }
    }
}
