package io.github.boronare.dragkeyboard.core

import kotlin.test.Test
import kotlin.test.assertEquals

class KeyboardEngineTest {
    private val text = { s: String -> KeyAction.Text(s) }
    private val backspace = KeyAction.Special(SpecialKey.BACKSPACE)

    @Test
    fun koreanLayoutComposes() {
        val engine = KeyboardEngine(listOf(DefaultLayouts.classic()))
        assertEquals(listOf(ImeCommand.Edit("", "ㄱ")), engine.perform(text("ㄱ")))
        assertEquals(listOf(ImeCommand.Edit("", "가")), engine.perform(text("ㅏ")))
        assertEquals(listOf(ImeCommand.Edit("", "ㄱ")), engine.perform(backspace))
    }

    @Test
    fun backspaceWithoutCompositionIsSentAsKey() {
        val engine = KeyboardEngine(listOf(DefaultLayouts.english()))
        assertEquals(listOf(ImeCommand.Edit("a", "")), engine.perform(text("a")))
        assertEquals(listOf(ImeCommand.SendSpecial(SpecialKey.BACKSPACE)), engine.perform(backspace))
    }

    @Test
    fun enterFlushesCompositionFirst() {
        val engine = KeyboardEngine(listOf(DefaultLayouts.classic()))
        engine.perform(text("ㄴ"))
        engine.perform(text("ㅏ"))
        assertEquals(
            listOf(ImeCommand.Edit("나", ""), ImeCommand.SendSpecial(SpecialKey.ENTER)),
            engine.perform(KeyAction.Special(SpecialKey.ENTER)),
        )
    }

    @Test
    fun layoutSwitchingWrapsAndChangesComposer() {
        val layouts = DefaultLayouts.all()
        val engine = KeyboardEngine(layouts)
        engine.perform(text("ㅎ"))

        val switched = engine.perform(KeyAction.SwitchLayout(LayoutTarget.NEXT))
        assertEquals(listOf(ImeCommand.Edit("ㅎ", ""), ImeCommand.LayoutChanged(1, layouts[1])), switched)
        assertEquals(listOf(ImeCommand.Edit("ㄱ", "")), engine.perform(text("ㄱ")))

        engine.perform(KeyAction.SwitchLayout(LayoutTarget.NEXT))
        assertEquals(0, engine.activeIndex)
        engine.perform(KeyAction.SwitchLayout(LayoutTarget.PREVIOUS))
        assertEquals(1, engine.activeIndex)
    }

    @Test
    fun activeIndexIsClamped() {
        val engine = KeyboardEngine(DefaultLayouts.all(), activeIndex = 7)
        assertEquals(1, engine.activeIndex)
    }

    @Test
    fun passwordModeTypesDubeolsikQwerty() {
        assertEquals("gks", Dubeolsik.toQwerty("한"))
        assertEquals("dkssudgktpdy", Dubeolsik.toQwerty("안녕하세요"))
        assertEquals("ekfr", Dubeolsik.toQwerty("닭"))
        assertEquals("rhk", Dubeolsik.toQwerty("과"))
        assertEquals("RTOP", Dubeolsik.toQwerty("ㄲㅆㅒㅖ"))
        assertEquals("abc1!", Dubeolsik.toQwerty("abc1!"))

        val engine = KeyboardEngine(listOf(DefaultLayouts.classic()))
        engine.passwordMode = true
        assertEquals(listOf(ImeCommand.Edit("g", "")), engine.perform(text("ㅎ")))
        assertEquals(listOf(ImeCommand.Edit("k", "")), engine.perform(text("ㅏ")))
        assertEquals(listOf(ImeCommand.SendSpecial(SpecialKey.BACKSPACE)), engine.perform(backspace))

        // 비밀번호 칸을 벗어나면 다시 한글을 조합한다
        engine.passwordMode = false
        assertEquals(listOf(ImeCommand.Edit("", "ㅎ")), engine.perform(text("ㅎ")))
    }
}
