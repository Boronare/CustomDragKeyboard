package io.github.boronare.dragkeyboard.core

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** 앱에 들어가는 실제 사전(app/src/main/assets/dict)으로 추천 품질과 속도를 확인한다. */
class BundledDictionaryTest {
    private fun load(name: String) =
        File("../app/src/main/assets/dict/$name.tsv").bufferedReader().useLines { WordDictionary.parse(it) }

    private val korean = load("ko")
    private val english = load("en")
    private val suggester = Suggester({ if (it == Language.KOREAN) korean else english }, UserHistory())

    @Test
    fun dictionariesAreLoaded() {
        assertTrue(korean.size > 20_000, "ko: ${korean.size}")
        assertTrue(english.size > 50_000, "en: ${english.size}")
    }

    @Test
    fun suggestsCommonWords() {
        assertTrue("사람" in suggester.suggest("사"), suggester.suggest("사").toString())
        assertTrue("하느님" in suggester.suggest("하느"), suggester.suggest("하느").toString())
        assertTrue("keyboard" in suggester.suggest("keybo"), suggester.suggest("keybo").toString())
        assertTrue("The" in suggester.suggest("Th"), suggester.suggest("Th").toString())
    }

    @Test
    fun lookupIsFastEvenForOneJamo() {
        // 가장 넓은 범위(자음 하나)도 키 입력 한 번 안에 끝나야 한다
        repeat(50) { suggester.suggest("ㅅ") }
        val start = System.nanoTime()
        repeat(200) { suggester.suggest("ㅅ"); suggester.suggest("s") }
        val perQueryMs = (System.nanoTime() - start) / 400 / 1e6
        assertTrue(perQueryMs < 5.0, "too slow: $perQueryMs ms per query")
    }
}
