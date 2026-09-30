package io.github.boronare.dragkeyboard.core

/** 입력기에 반영할 변화: [commit]을 확정한 뒤 [composing]을 조합 중 글자로 둔다. */
data class CompositionUpdate(val commit: String, val composing: String)

interface Composer {
    /** 현재 조합 중인 글자. 없으면 빈 문자열. */
    val composing: String

    fun input(text: String): CompositionUpdate

    /** 조합 중인 글자를 한 단계 지운다. 조합 중인 글자가 없으면 null. */
    fun backspace(): CompositionUpdate?

    /** 조합 중인 글자를 확정하고 반환한다. */
    fun flush(): String

    fun reset()
}

/** 조합 없이 입력을 그대로 확정한다 (영어 등). */
class PassthroughComposer : Composer {
    override val composing: String get() = ""
    override fun input(text: String) = CompositionUpdate(text, "")
    override fun backspace(): CompositionUpdate? = null
    override fun flush() = ""
    override fun reset() {}
}

/**
 * 호환용 한글 자모(ㄱ, ㅏ …)를 받아 음절로 조합하는 두벌식 오토마타.
 * 된소리(ㄲ 등)는 자판에서 바로 입력하므로 같은 자음 두 번을 합치지는 않는다.
 * 백스페이스는 입력한 순서를 거꾸로 되돌린다 (닭 → 달 → 다 → ㄷ).
 */
class HangulComposer : Composer {
    private data class Syllable(val cho: Int = -1, val jung: Int = -1, val jong: Int = 0)

    // 현재 음절이 만들어져 온 상태들. 마지막이 현재 상태다.
    private val history = ArrayDeque<Syllable>()

    override val composing: String
        get() = history.lastOrNull()?.let(::render) ?: ""

    override fun input(text: String): CompositionUpdate {
        val ch = text.singleOrNull()
        return when {
            ch == null -> CompositionUpdate(flush() + text, "")
            JUNG.indexOf(ch) >= 0 -> inputVowel(JUNG.indexOf(ch))
            ch in CONSONANTS -> inputConsonant(ch)
            else -> CompositionUpdate(flush() + text, "")
        }
    }

    private fun inputConsonant(ch: Char): CompositionUpdate {
        val s = history.lastOrNull()
        val jong = JONG.indexOf(ch).takeIf { it > 0 }
        return when {
            s == null || s.cho < 0 || s.jung < 0 -> startWithConsonant(ch)
            s.jong == 0 && jong != null -> push(s.copy(jong = jong))
            s.jong > 0 -> {
                val combined = JONG_COMBINE["${JONG[s.jong]}$ch"]
                if (combined != null) push(s.copy(jong = JONG.indexOf(combined))) else startWithConsonant(ch)
            }
            else -> startWithConsonant(ch)
        }
    }

    private fun inputVowel(jung: Int): CompositionUpdate {
        val s = history.lastOrNull()
        return when {
            s == null -> push(Syllable(jung = jung))
            s.jung < 0 -> push(s.copy(jung = jung))
            s.jong == 0 -> {
                val combined = JUNG_COMBINE["${JUNG[s.jung]}${JUNG[jung]}"]
                if (combined != null) {
                    push(s.copy(jung = JUNG.indexOf(combined)))
                } else {
                    val commit = flush()
                    push(Syllable(jung = jung)).copy(commit = commit)
                }
            }
            else -> {
                // 받침이 다음 글자의 초성으로 넘어간다: 각 + ㅏ → 가가, 닭 + ㅏ → 달가
                val jongChar = JONG[s.jong]
                val split = JONG_SPLIT[jongChar]
                val keep = split?.let { JONG.indexOf(it.first) } ?: 0
                val moved = CHO.indexOf(split?.second ?: jongChar)
                val commit = render(s.copy(jong = keep))
                history.clear()
                history.addLast(Syllable(cho = moved))
                push(Syllable(cho = moved, jung = jung)).copy(commit = commit)
            }
        }
    }

    private fun startWithConsonant(ch: Char): CompositionUpdate {
        val commit = flush()
        val cho = CHO.indexOf(ch)
        if (cho < 0) return CompositionUpdate(commit + ch, "") // ㄳ 같은 겹자음은 초성이 될 수 없다
        return push(Syllable(cho = cho)).copy(commit = commit)
    }

    private fun push(s: Syllable): CompositionUpdate {
        history.addLast(s)
        return CompositionUpdate("", composing)
    }

    override fun backspace(): CompositionUpdate? {
        if (history.isEmpty()) return null
        history.removeLast()
        return CompositionUpdate("", composing)
    }

    override fun flush(): String {
        val text = composing
        history.clear()
        return text
    }

    override fun reset() = history.clear()

    private fun render(s: Syllable): String = when {
        s.cho >= 0 && s.jung >= 0 -> (0xAC00 + (s.cho * 21 + s.jung) * 28 + s.jong).toChar().toString()
        s.cho >= 0 -> CHO[s.cho].toString()
        s.jung >= 0 -> JUNG[s.jung].toString()
        else -> ""
    }

    companion object {
        private const val CHO = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
        private const val JUNG = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
        private const val JONG = " ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ" // 0번은 받침 없음
        private val CONSONANTS = ('ㄱ'..'ㅎ').toSet()

        private val JUNG_COMBINE = mapOf(
            "ㅗㅏ" to 'ㅘ', "ㅗㅐ" to 'ㅙ', "ㅗㅣ" to 'ㅚ',
            "ㅜㅓ" to 'ㅝ', "ㅜㅔ" to 'ㅞ', "ㅜㅣ" to 'ㅟ',
            "ㅡㅣ" to 'ㅢ',
        )
        private val JONG_COMBINE = mapOf(
            "ㄱㅅ" to 'ㄳ', "ㄴㅈ" to 'ㄵ', "ㄴㅎ" to 'ㄶ',
            "ㄹㄱ" to 'ㄺ', "ㄹㅁ" to 'ㄻ', "ㄹㅂ" to 'ㄼ', "ㄹㅅ" to 'ㄽ',
            "ㄹㅌ" to 'ㄾ', "ㄹㅍ" to 'ㄿ', "ㄹㅎ" to 'ㅀ', "ㅂㅅ" to 'ㅄ',
        )
        private val JONG_SPLIT = JONG_COMBINE.entries.associate { (pair, combined) -> combined to (pair[0] to pair[1]) }
    }
}
