package io.github.boronare.dragkeyboard.ime

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import io.github.boronare.dragkeyboard.core.DefaultLayouts
import io.github.boronare.dragkeyboard.core.ImeCommand
import io.github.boronare.dragkeyboard.core.KeyAction
import io.github.boronare.dragkeyboard.core.KeyboardEngine
import io.github.boronare.dragkeyboard.core.SpecialKey
import io.github.boronare.dragkeyboard.data.KeyboardPrefs
import io.github.boronare.dragkeyboard.data.LayoutRepository
import io.github.boronare.dragkeyboard.data.PrefsStore

class DragKeyboardService : InputMethodService() {
    private lateinit var repository: LayoutRepository
    private lateinit var prefsStore: PrefsStore
    private val engine = KeyboardEngine(DefaultLayouts.all())
    private var prefs = KeyboardPrefs()
    private var loadedStamp = Long.MIN_VALUE
    private var keyboardView: DragKeyboardView? = null

    override fun onCreate() {
        super.onCreate()
        repository = LayoutRepository(this)
        prefsStore = PrefsStore(this)
        reload()
    }

    override fun onCreateInputView(): View = DragKeyboardView(this).also {
        it.onAction = ::onKeyAction
        keyboardView = it
        it.bind(engine.activeLayout, prefs)
    }

    // 가로 모드에서도 전체 화면 입력창 대신 앱 화면 위에 키보드를 띄운다
    override fun onEvaluateFullscreenMode() = false

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        reload()
        engine.reset()
        keyboardView?.bind(engine.activeLayout, prefs)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        currentInputConnection?.finishComposingText()
        engine.reset()
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
    }

    /** 설정 앱에서 바뀐 자판과 환경설정을 반영한다. */
    private fun reload() {
        prefs = prefsStore.load()
        val stamp = repository.lastModified
        if (stamp == loadedStamp) return
        loadedStamp = stamp
        val layouts = repository.load()
        val active = layouts.indexOfFirst { it.id == prefsStore.activeLayoutId }.coerceAtLeast(0)
        engine.setLayouts(layouts, active)
    }

    private fun onKeyAction(action: KeyAction) {
        val ic = currentInputConnection ?: return
        engine.perform(action).forEach { execute(it, ic) }
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

    /** 검색창 등에서는 줄바꿈 대신 입력창이 요구하는 동작(검색, 보내기 …)을 실행한다. */
    private fun sendEnter(ic: InputConnection) {
        val options = currentInputEditorInfo?.imeOptions ?: 0
        val action = options and EditorInfo.IME_MASK_ACTION
        val hasAction = (options and EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0 &&
            action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED
        if (hasAction) ic.performEditorAction(action) else sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }

    private fun switchInputMethod() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && switchToNextInputMethod(false)) return
        getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
    }
}
