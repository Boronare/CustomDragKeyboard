package io.github.boronare.dragkeyboard.core

/** 엔진이 입력기 서비스에 요청하는 동작. */
sealed interface ImeCommand {
    data class Edit(val commit: String, val composing: String) : ImeCommand
    data class SendSpecial(val key: SpecialKey) : ImeCommand
    data class SendKeyCode(val code: Int) : ImeCommand
    data object SwitchInputMethod : ImeCommand
    data class LayoutChanged(val index: Int, val layout: KeyboardLayout) : ImeCommand
}

/**
 * 키 동작을 받아 입력기 명령으로 바꾼다. Android에 의존하지 않아 단위 테스트가 가능하다.
 */
class KeyboardEngine(layouts: List<KeyboardLayout>, activeIndex: Int = 0) {
    var layouts: List<KeyboardLayout> = emptyList()
        private set
    var activeIndex: Int = 0
        private set
    private var composer: Composer = PassthroughComposer()

    val activeLayout: KeyboardLayout get() = layouts[activeIndex]
    val composingText: String get() = composer.composing
    val isComposing: Boolean get() = composingText.isNotEmpty()

    init {
        setLayouts(layouts, activeIndex)
    }

    /** 자판 목록을 교체한다. 조합 중이던 글자는 버리므로 호출 전에 입력기 쪽 조합을 끝내야 한다. */
    fun setLayouts(layouts: List<KeyboardLayout>, activeIndex: Int) {
        require(layouts.isNotEmpty()) { "at least one layout is required" }
        this.layouts = layouts
        select(activeIndex.coerceIn(layouts.indices))
    }

    fun perform(action: KeyAction): List<ImeCommand> = when (action) {
        is KeyAction.Text -> listOf(composer.input(action.text).toCommand())
        is KeyAction.Special ->
            if (action.key == SpecialKey.BACKSPACE) {
                listOf(composer.backspace()?.toCommand() ?: ImeCommand.SendSpecial(SpecialKey.BACKSPACE))
            } else {
                flushCommands() + ImeCommand.SendSpecial(action.key)
            }
        is KeyAction.KeyCode -> flushCommands() + ImeCommand.SendKeyCode(action.code)
        is KeyAction.SwitchInputMethod -> flushCommands() + ImeCommand.SwitchInputMethod
        is KeyAction.SwitchLayout -> {
            val flushed = flushCommands()
            val step = if (action.target == LayoutTarget.NEXT) 1 else -1
            select(Math.floorMod(activeIndex + step, layouts.size))
            flushed + ImeCommand.LayoutChanged(activeIndex, activeLayout)
        }
    }

    /** 커서 이동 등으로 조합이 끊겼을 때 상태를 버린다. */
    fun reset() = composer.reset()

    private fun select(index: Int) {
        activeIndex = index
        composer = when (activeLayout.language) {
            Language.KOREAN -> HangulComposer()
            Language.ENGLISH -> PassthroughComposer()
        }
    }

    private fun flushCommands(): List<ImeCommand> {
        val text = composer.flush()
        return if (text.isEmpty()) emptyList() else listOf(ImeCommand.Edit(text, ""))
    }

    private fun CompositionUpdate.toCommand() = ImeCommand.Edit(commit, composing)
}
