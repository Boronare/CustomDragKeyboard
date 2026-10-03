package com.naremotion.dragkeyboard.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HangulComposerTest {
    /** 입력기 화면을 흉내낸다: 확정된 글자 + 조합 중인 글자. */
    private class Screen {
        val composer = HangulComposer()
        val committed = StringBuilder()
        val text get() = committed.toString() + composer.composing

        fun type(keys: String) = apply {
            keys.forEach { committed.append(composer.input(it.toString()).commit) }
        }

        fun backspace() = apply {
            if (composer.backspace() == null && committed.isNotEmpty()) committed.setLength(committed.length - 1)
        }
    }

    private fun typed(keys: String) = Screen().type(keys).text

    @Test
    fun composesSyllables() {
        assertEquals("한글", typed("ㅎㅏㄴㄱㅡㄹ"))
        assertEquals("드래그", typed("ㄷㅡㄹㅐㄱㅡ"))
        assertEquals("키보드", typed("ㅋㅣㅂㅗㄷㅡ"))
    }

    @Test
    fun finalConsonantMovesToNextSyllable() {
        assertEquals("가나", typed("ㄱㅏㄴㅏ"))
        assertEquals("달가", typed("ㄷㅏㄹㄱㅏ"))
        assertEquals("갑시", typed("ㄱㅏㅂㅅㅣ"))
    }

    @Test
    fun compoundJamo() {
        assertEquals("닭", typed("ㄷㅏㄹㄱ"))
        assertEquals("값", typed("ㄱㅏㅂㅅ"))
        assertEquals("괜찮", typed("ㄱㅗㅐㄴㅊㅏㄴㅎ"))
        assertEquals("의사", typed("ㅇㅡㅣㅅㅏ"))
        assertEquals("뭐", typed("ㅁㅜㅓ"))
    }

    @Test
    fun doubleConsonantsTypedDirectly() {
        assertEquals("까치", typed("ㄲㅏㅊㅣ"))
        assertEquals("있다", typed("ㅇㅣㅆㄷㅏ"))
        // ㄸ은 받침이 될 수 없으므로 다음 글자의 초성이 된다
        assertEquals("아따", typed("ㅇㅏㄸㅏ"))
    }

    @Test
    fun loneJamo() {
        assertEquals("ㅋㅋㅋ", typed("ㅋㅋㅋ"))
        assertEquals("ㅏㅏ", typed("ㅏㅏ"))
        assertEquals("ㅠㅠ", typed("ㅠㅠ"))
        assertEquals("ㅏ가", typed("ㅏㄱㅏ"))
    }

    @Test
    fun otherTextFlushesComposition() {
        assertEquals("한 글!", typed("ㅎㅏㄴ ㄱㅡㄹ!"))
        assertEquals("abc가", typed("abcㄱㅏ"))
    }

    @Test
    fun backspaceUndoesJamoInInputOrder() {
        val screen = Screen().type("ㄷㅏㄹㄱ")
        assertEquals("달", screen.backspace().text)
        assertEquals("다", screen.backspace().text)
        assertEquals("ㄷ", screen.backspace().text)
        assertEquals("", screen.backspace().text)
    }

    @Test
    fun backspaceAfterSplitKeepsPreviousSyllable() {
        val screen = Screen().type("ㄱㅏㄱㅏ")
        assertEquals("가ㄱ", screen.backspace().text)
        assertEquals("가", screen.backspace().text)
        // 조합이 끝났으므로 나머지는 입력기가 일반 삭제로 처리한다
        assertNull(screen.composer.backspace())
    }

    @Test
    fun compoundVowelBackspace() {
        val screen = Screen().type("ㄱㅗㅏ")
        assertEquals("과", screen.text)
        assertEquals("고", screen.backspace().text)
    }

    @Test
    fun flushCommitsComposition() {
        val composer = HangulComposer()
        composer.input("ㄱ")
        composer.input("ㅏ")
        assertEquals("가", composer.flush())
        assertEquals("", composer.composing)
    }
}
