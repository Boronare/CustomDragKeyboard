package com.naremotion.dragkeyboard.core

import kotlin.math.ln

/**
 * 빈도 사전. 정규화한 키([Jamo.normalize])로 정렬한 배열에서 이진 탐색으로 접두어 범위를 찾는다.
 * 키를 누를 때마다 DB를 조회하던 원작과 달리 메모리에서 바로 찾으므로 입력이 끊기지 않는다.
 */
class WordDictionary(entries: Collection<Pair<String, Int>>) {
    private val keys: Array<String>
    private val words: Array<String>
    private val frequencies: IntArray

    init {
        val sorted = entries
            .groupBy { it.first }
            .map { (word, list) -> Triple(Jamo.normalize(word), word, list.maxOf { it.second }) }
            .sortedBy { it.first }
        keys = Array(sorted.size) { sorted[it].first }
        words = Array(sorted.size) { sorted[it].second }
        frequencies = IntArray(sorted.size) { sorted[it].third }
    }

    val size: Int get() = words.size

    /** [normalizedPrefix]로 시작하는 단어 중 빈도가 높은 [limit]개. */
    fun lookup(normalizedPrefix: String, limit: Int): List<Pair<String, Int>> {
        if (normalizedPrefix.isEmpty()) return emptyList()
        var i = lowerBound(normalizedPrefix)
        val top = ArrayList<Pair<String, Int>>(limit + 1)
        while (i < keys.size && keys[i].startsWith(normalizedPrefix)) {
            val f = frequencies[i]
            if (top.size < limit || f > top.last().second) {
                top.add(words[i] to f)
                top.sortByDescending { it.second }
                if (top.size > limit) top.removeAt(top.lastIndex)
            }
            i++
        }
        return top
    }

    private fun lowerBound(key: String): Int {
        var lo = 0
        var hi = keys.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (keys[mid] < key) lo = mid + 1 else hi = mid
        }
        return lo
    }

    companion object {
        /** "단어<TAB>빈도" 형식의 줄들을 읽는다. 형식이 맞지 않는 줄은 건너뛴다. */
        fun parse(lines: Sequence<String>): WordDictionary = WordDictionary(
            lines.mapNotNull { line ->
                val tab = line.indexOf('\t')
                if (tab <= 0) return@mapNotNull null
                val freq = line.substring(tab + 1).trim().toIntOrNull() ?: return@mapNotNull null
                line.substring(0, tab) to freq
            }.toList(),
        )
    }
}

/** 사용자가 실제로 입력한 단어의 횟수. 사전에 없는 말투·활용형("했어요")도 이걸로 배운다. */
class UserHistory(initial: Map<String, Int> = emptyMap()) {
    private val counts = HashMap(initial)
    var dirty = false
        private set

    val entries: Map<String, Int> get() = counts

    fun learn(word: String) {
        if (!isLearnable(word)) return
        counts[word] = (counts[word] ?: 0) + 1
        dirty = true
        if (counts.size > MAX_WORDS) {
            // 가장 적게 쓴 단어부터 정리한다
            counts.entries.sortedBy { it.value }.take(counts.size - MAX_WORDS).forEach { counts.remove(it.key) }
        }
    }

    fun clear() {
        counts.clear()
        dirty = true
    }

    fun encode(): String = counts.entries.joinToString("\n") { "${it.key}\t${it.value}" }

    fun markSaved() {
        dirty = false
    }

    companion object {
        const val MAX_WORDS = 5000

        fun decode(text: String) = UserHistory(
            text.lineSequence().mapNotNull { line ->
                val tab = line.indexOf('\t')
                if (tab <= 0) return@mapNotNull null
                val count = line.substring(tab + 1).toIntOrNull() ?: return@mapNotNull null
                line.substring(0, tab) to count
            }.toMap(),
        )

        fun isLearnable(word: String) = word.length in 2..32 && word.all { it.isLetter() || it == '\'' }
    }
}

/**
 * 입력 중인 단어에 대한 추천을 만든다. 업계 키보드들과 같은 구조로
 * 기본 빈도 사전 + 사용자 입력 학습을 합쳐 점수를 매긴다.
 */
class Suggester(
    private val dictionaryFor: (Language) -> WordDictionary?,
    val history: UserHistory,
) {
    fun suggest(prefix: String, limit: Int = 3): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val language = if (prefix.any(Jamo::isHangul)) Language.KOREAN else Language.ENGLISH
        val key = Jamo.normalize(prefix)
        val scores = HashMap<String, Double>()
        dictionaryFor(language)?.lookup(key, limit * 4)?.forEach { (word, freq) ->
            scores[word] = freq / 100.0
        }
        history.entries.forEach { (word, count) ->
            if (Jamo.normalize(word).startsWith(key)) {
                scores[word] = (scores[word] ?: USER_BASE) + USER_WEIGHT * ln(1.0 + count)
            }
        }
        return scores.entries
            .filter { Jamo.normalize(it.key) != key }
            // 받침이 다음 글자로 넘어가는 후보(삼 → 사무실)보다 같은 글자 안에서 이어지는 후보(삼 → 삼성, 하 → 한국)를 앞에 둔다
            .sortedByDescending { it.value + if (staysInSyllables(prefix, it.key)) SAME_SYLLABLE_BONUS else 0.0 }
            .take(limit)
            .map { matchCase(prefix, it.key) }
    }

    private fun staysInSyllables(prefix: String, word: String): Boolean {
        if (word.length < prefix.length) return false
        val last = prefix.lastIndex
        for (i in 0 until last) {
            if (!word[i].equals(prefix[i], ignoreCase = true)) return false
        }
        return Jamo.normalize(word[last].toString()).startsWith(Jamo.normalize(prefix[last].toString()))
    }

    /** "Key" → "Keyboard"처럼 첫 글자 대문자를 따라간다. */
    private fun matchCase(prefix: String, word: String): String =
        if (prefix.first().isUpperCase() && word.first().isLowerCase()) {
            word.replaceFirstChar { it.uppercaseChar() }
        } else {
            word
        }

    private companion object {
        // 사전 점수는 Zipf 빈도(대략 1~8, 흔한 낱말이 6 안팎)다.
        // 사전에 없는 학습 단어는 USER_BASE에서 시작해, 세 번 쓰면 흔한 낱말 정도(약 6.6)가 된다.
        const val USER_BASE = 4.5
        const val USER_WEIGHT = 1.5
        const val SAME_SYLLABLE_BONUS = 1.0
    }
}
