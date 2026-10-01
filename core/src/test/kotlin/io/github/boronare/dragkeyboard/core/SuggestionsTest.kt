package io.github.boronare.dragkeyboard.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SuggestionsTest {
    private val korean = WordDictionary(
        listOf("하나" to 550, "하늘" to 500, "하루" to 520, "한국" to 600, "학교" to 580, "과자" to 400, "닭고기" to 380),
    )
    private val english = WordDictionary(listOf("keyboard" to 411, "key" to 500, "keep" to 520, "the" to 773))

    private fun suggester(history: UserHistory = UserHistory()) = Suggester(
        { if (it == Language.KOREAN) korean else english },
        history,
    )

    @Test
    fun jamoNormalization() {
        assertEquals("ㅎㅏㄴㄱㅜㄱ", Jamo.normalize("한국"))
        assertEquals("ㄱㅗㅏㅈㅏ", Jamo.normalize("과자"))
        assertEquals("ㄷㅏㄹㄱㄱㅗㄱㅣ", Jamo.normalize("닭고기"))
        assertEquals("abc", Jamo.normalize("AbC"))
    }

    @Test
    fun composingSyllableMatchesNextSyllable() {
        // "한"을 치는 중이면 받침 ㄴ이 넘어간 "하나", "하늘"도 후보다
        assertEquals(listOf("한국", "하나", "하늘"), suggester().suggest("한"))
        assertEquals(listOf("한국", "학교", "하나"), suggester().suggest("하"))
        assertEquals(listOf("과자"), suggester().suggest("고"))
        assertEquals(listOf("닭고기"), suggester().suggest("달"))
    }

    @Test
    fun sameSyllableContinuationsComeFirst() {
        val dict = WordDictionary(listOf("사망" to 520, "사무실" to 510, "삼성" to 480))
        val s = Suggester({ dict }, UserHistory())
        // 사망·사무실이 더 흔해도, 화면의 "삼"을 그대로 잇는 삼성이 먼저다
        assertEquals("삼성", s.suggest("삼").first())
    }

    @Test
    fun typedWordIsNotSuggested() {
        assertEquals(listOf("keyboard"), suggester().suggest("key"))
        assertEquals(listOf("keep", "key", "keyboard"), suggester().suggest("ke"))
    }

    @Test
    fun followsCapitalization() {
        assertEquals("Keyboard", suggester().suggest("Keyb").single())
    }

    @Test
    fun learnedWordsAreSuggestedAndRanked() {
        val history = UserHistory()
        repeat(3) { history.learn("하드코딩") }
        history.learn("했어요")
        val s = suggester(history)
        assertEquals("하드코딩", s.suggest("하").first())
        assertEquals(listOf("했어요"), s.suggest("했"))
    }

    @Test
    fun historyRoundTripAndLimits() {
        val history = UserHistory()
        history.learn("안녕")
        history.learn("안녕")
        history.learn("a")       // 너무 짧음
        history.learn("1234")    // 글자가 아님
        assertTrue(history.dirty)
        val decoded = UserHistory.decode(history.encode())
        assertEquals(mapOf("안녕" to 2), decoded.entries)
        assertFalse(decoded.dirty)
    }

    @Test
    fun parsesDictionaryFile() {
        val dict = WordDictionary.parse(sequenceOf("하늘\t500", "broken", "\t3", "하나\tx", "하루\t520"))
        assertEquals(2, dict.size)
        assertEquals(listOf("하루" to 520, "하늘" to 500), dict.lookup(Jamo.normalize("하"), 5))
    }
}
