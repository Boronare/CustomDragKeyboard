package io.github.boronare.dragkeyboard.ime

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import io.github.boronare.dragkeyboard.core.DefaultLayouts
import io.github.boronare.dragkeyboard.core.ImeCommand
import io.github.boronare.dragkeyboard.core.KeyAction
import io.github.boronare.dragkeyboard.core.KeyboardEngine
import io.github.boronare.dragkeyboard.core.KeyboardLayout
import io.github.boronare.dragkeyboard.core.SpecialKey
import io.github.boronare.dragkeyboard.core.Suggester
import io.github.boronare.dragkeyboard.core.UserHistory
import io.github.boronare.dragkeyboard.data.KeyboardPrefs
import io.github.boronare.dragkeyboard.data.LayoutRepository
import io.github.boronare.dragkeyboard.data.PrefsStore
import io.github.boronare.dragkeyboard.data.WordStore

class DragKeyboardService : InputMethodService() {
    private lateinit var repository: LayoutRepository
    private lateinit var prefsStore: PrefsStore
    private lateinit var wordStore: WordStore
    private lateinit var suggester: Suggester
    private var history = UserHistory()
    private var historyStamp = Long.MIN_VALUE
    private val engine = KeyboardEngine(DefaultLayouts.all())
    private var prefs = KeyboardPrefs()
    private var loadedStamp = Long.MIN_VALUE
    private var userLayouts: List<KeyboardLayout> = DefaultLayouts.all()
    private var keyboardView: DragKeyboardView? = null
    private var stripView: SuggestionStripView? = null

    // 현재 입력창에서 추천과 학습을 해도 되는지
    private var suggesting = false
    private var learning = false

    override fun onCreate() {
        super.onCreate()
        repository = LayoutRepository(this)
        prefsStore = PrefsStore(this)
        wordStore = WordStore(this)
        reload()
    }

    override fun onCreateInputView(): View {
        val strip = SuggestionStripView(this).also {
            it.onPick = ::pickSuggestion
            stripView = it
        }
        val keyboard = DragKeyboardView(this).also {
            it.onAction = ::onKeyAction
            keyboardView = it
            it.bind(engine.activeLayout, prefs)
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(strip, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            addView(keyboard, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    // 가로 모드에서도 전체 화면 입력창 대신 앱 화면 위에 키보드를 띄운다
    override fun onEvaluateFullscreenMode() = false

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        reload()
        engine.reset()
        // 숫자·전화번호·날짜 입력칸에는 사용자 자판 대신 3x4 키패드를 띄운다
        val pad = numericPadFor(info)
        if (pad != null) {
            engine.setLayouts(listOf(pad), 0)
        } else {
            engine.setLayouts(userLayouts, userLayouts.indexOfFirst { it.id == prefsStore.activeLayoutId }.coerceAtLeast(0))
        }
        engine.passwordMode = isPassword(info)
        suggesting = prefs.suggestions && allowsSuggestions(info)
        learning = suggesting && prefs.learnWords &&
            ((info?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) == 0
        if (suggesting) wordStore.preload(engine.activeLayout.language)
        keyboardView?.bind(engine.activeLayout, prefs)
        keyboardView?.enterLabel = enterLabelFor(info)
        stripView?.visibility = if (suggesting) View.VISIBLE else View.GONE
        updateSuggestions()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        currentInputConnection?.finishComposingText()
        engine.reset()
        saveHistory()
        super.onFinishInputView(finishingInput)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int,
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        // 사용자가 커서를 옮기면 조합을 끝낸다
        if (engine.isComposing && (newSelStart != newSelEnd || newSelEnd != candidatesEnd)) {
            engine.reset()
            currentInputConnection?.finishComposingText()
        }
        updateSuggestions()
    }

    /** 설정 앱에서 바뀐 자판, 환경설정, 학습 기록을 반영한다. */
    private fun reload() {
        prefs = prefsStore.load()
        val stamp = repository.lastModified
        if (stamp != loadedStamp) {
            loadedStamp = stamp
            userLayouts = repository.load()
        }
        if (wordStore.historyStamp != historyStamp) {
            historyStamp = wordStore.historyStamp
            history = wordStore.loadHistory()
            suggester = Suggester(wordStore::dictionary, history)
        }
    }

    private fun saveHistory() {
        if (!history.dirty) return
        if (wordStore.historyStamp != historyStamp) {
            // 그 사이 설정 앱에서 학습 기록을 지웠다. 들고 있던 기록은 버린다.
            reload()
            return
        }
        wordStore.saveHistory(history)
        historyStamp = wordStore.historyStamp
    }

    private fun onKeyAction(action: KeyAction) {
        val ic = currentInputConnection ?: return
        // 단어가 끝나는 순간(띄어쓰기, 문장부호, 엔터) 그 단어를 배운다
        if (learning && endsWord(action)) history.learn(currentWord(ic))
        engine.perform(action).forEach { execute(it, ic) }
        updateSuggestions()
    }

    private fun endsWord(action: KeyAction): Boolean = when (action) {
        is KeyAction.Text -> !action.text.first().let { it.isLetter() || it == '\'' }
        is KeyAction.Special -> action.key == SpecialKey.ENTER || action.key == SpecialKey.TAB
        else -> false
    }

    /** 커서 바로 앞의 단어 (조합 중인 글자 포함). */
    private fun currentWord(ic: InputConnection): String {
        val before = ic.getTextBeforeCursor(MAX_WORD_LENGTH, 0) ?: return ""
        var start = before.length
        while (start > 0 && before[start - 1].let { it.isLetter() || it == '\'' }) start--
        return before.substring(start)
    }

    private fun updateSuggestions() {
        val strip = stripView ?: return
        val ic = currentInputConnection
        if (!suggesting || ic == null) {
            strip.setSuggestions(emptyList())
            return
        }
        strip.setSuggestions(suggester.suggest(currentWord(ic)))
    }

    /** 입력 중인 단어를 추천 단어로 바꾸고 한 칸 띄운다. */
    private fun pickSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val typed = currentWord(ic)
        val composing = engine.composingText
        engine.reset()
        ic.beginBatchEdit()
        ic.setComposingText("", 1)
        ic.finishComposingText()
        ic.deleteSurroundingText((typed.length - composing.length).coerceAtLeast(0), 0)
        ic.commitText("$word ", 1)
        ic.endBatchEdit()
        if (learning) history.learn(word)
        updateSuggestions()
    }

    private fun execute(command: ImeCommand, ic: InputConnection) {
        when (command) {
            is ImeCommand.Edit -> {
                ic.beginBatchEdit()
                if (command.commit.isNotEmpty()) ic.commitText(command.commit, 1)
                if (command.composing.isNotEmpty()) {
                    ic.setComposingText(command.composing, 1)
                } else if (command.commit.isEmpty()) {
                    ic.setComposingText("", 1)
                    ic.finishComposingText()
                }
                ic.endBatchEdit()
            }
            is ImeCommand.SendSpecial -> sendSpecial(command.key, ic)
            is ImeCommand.SendKeyCode -> sendDownUpKeyEvents(command.code)
            ImeCommand.SwitchInputMethod -> switchInputMethod()
            is ImeCommand.LayoutChanged -> {
                prefsStore.activeLayoutId = command.layout.id
                keyboardView?.bind(command.layout, prefs)
                if (suggesting) wordStore.preload(command.layout.language)
            }
        }
    }

    private fun sendSpecial(key: SpecialKey, ic: InputConnection) {
        when (key) {
            SpecialKey.BACKSPACE -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            SpecialKey.ENTER -> sendEnter(ic)
            SpecialKey.TAB -> sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB)
            SpecialKey.CURSOR_LEFT -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
            SpecialKey.CURSOR_RIGHT -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
            SpecialKey.FORWARD_DELETE -> sendDownUpKeyEvents(KeyEvent.KEYCODE_FORWARD_DEL)
        }
    }

    /** 입력창이 엔터 대신 요구하는 동작(검색, 보내기 …). 없으면 null이고 줄바꿈을 보낸다. */
    private fun editorAction(info: EditorInfo?): Int? {
        val options = info?.imeOptions ?: return null
        if ((options and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) return null
        val action = options and EditorInfo.IME_MASK_ACTION
        return action.takeUnless { it == EditorInfo.IME_ACTION_NONE || it == EditorInfo.IME_ACTION_UNSPECIFIED }
    }

    private fun sendEnter(ic: InputConnection) {
        val action = editorAction(currentInputEditorInfo)
        if (action != null) ic.performEditorAction(action) else sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }

    private fun enterLabelFor(info: EditorInfo?): String? = when (editorAction(info)) {
        EditorInfo.IME_ACTION_SEARCH -> "🔍"
        EditorInfo.IME_ACTION_SEND -> "➤"
        EditorInfo.IME_ACTION_GO -> "➜"
        EditorInfo.IME_ACTION_NEXT -> "⇥"
        EditorInfo.IME_ACTION_PREVIOUS -> "⇤"
        EditorInfo.IME_ACTION_DONE -> "✓"
        else -> null
    }

    private fun numericPadFor(info: EditorInfo?): KeyboardLayout? =
        when ((info?.inputType ?: 0) and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_DATETIME -> DefaultLayouts.numberPad()
            InputType.TYPE_CLASS_PHONE -> DefaultLayouts.phonePad()
            else -> null
        }

    private fun isPassword(info: EditorInfo?): Boolean {
        val type = info?.inputType ?: return false
        if ((type and InputType.TYPE_MASK_CLASS) != InputType.TYPE_CLASS_TEXT) return false
        val variation = type and InputType.TYPE_MASK_VARIATION
        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
    }

    /** 비밀번호나 숫자 입력창에서는 추천하지도 배우지도 않는다. */
    private fun allowsSuggestions(info: EditorInfo?): Boolean {
        val type = info?.inputType ?: return false
        return (type and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT && !isPassword(info)
    }

    private fun switchInputMethod() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && switchToNextInputMethod(false)) return
        getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
    }

    private companion object {
        const val MAX_WORD_LENGTH = 48
    }
}
