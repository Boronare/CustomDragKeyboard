package com.naremotion.dragkeyboard.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LayoutTest {
    @Test
    fun defaultLayoutsAreComplete() {
        DefaultLayouts.all().forEach { layout ->
            assertEquals(3, layout.rows)
            assertEquals(5, layout.columns)
            assertTrue(layout.keys.none { it.isEmpty }, "${layout.name} has an empty key")
            val actions = layout.keys.flatMap { it.actions }
            assertTrue(KeyAction.Special(SpecialKey.BACKSPACE) in actions)
            assertTrue(KeyAction.Special(SpecialKey.ENTER) in actions)
            assertTrue(KeyAction.Text(" ") in actions)
            assertTrue(KeyAction.SwitchLayout(LayoutTarget.NEXT) in actions)
        }
    }

    @Test
    fun numericPadsAreStandardKeypads() {
        listOf(DefaultLayouts.numberPad(), DefaultLayouts.phonePad()).forEach { pad ->
            assertEquals(4, pad.rows)
            // 3x4 숫자 배열: 1 2 3 / 4 5 6 / 7 8 9 / _ 0 _
            val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
            digits.forEachIndexed { i, d ->
                assertEquals(KeyAction.Text(d), pad.key(i / 3, i % 3)[Direction.TAP])
            }
            assertEquals(KeyAction.Text("0"), pad.key(3, 1)[Direction.TAP])
            val actions = pad.keys.flatMap { it.actions }
            assertTrue(KeyAction.Special(SpecialKey.BACKSPACE) in actions)
            assertTrue(KeyAction.Special(SpecialKey.ENTER) in actions)
        }
    }

    @Test
    fun resizeKeepsOverlappingKeys() {
        val classic = DefaultLayouts.classic()
        val bigger = classic.resized(4, 6)
        assertEquals(classic.key(2, 4), bigger.key(2, 4))
        assertTrue(bigger.key(3, 5).isEmpty)
        val smaller = bigger.resized(2, 3)
        assertEquals(classic.key(1, 2), smaller.key(1, 2))
        assertEquals(6, smaller.keys.size)
    }

    @Test
    fun codecRoundTrip() {
        val layouts = DefaultLayouts.all()
        val json = LayoutCodec.encode(layouts)
        assertEquals(layouts, LayoutCodec.decode(json))
        assertTrue("\"type\": \"special\"" in json)
    }

    @Test
    fun customLabelsSurviveRoundTrip() {
        val key = KeySpec.EMPTY
            .with(Direction.TAP, KeyAction.KeyCode(111, label = "Esc"))
            .with(Direction.N, KeyAction.Text("안녕하세요", label = "인사"))
        val layout = KeyboardLayout.blank("Test", Language.ENGLISH, 1, 1).withKey(0, 0, key)
        val decoded = LayoutCodec.decode(LayoutCodec.encode(listOf(layout))).single()
        assertEquals("Esc", decoded.key(0, 0)[Direction.TAP]?.displayLabel)
        assertEquals("인사", decoded.key(0, 0)[Direction.N]?.displayLabel)
    }

    @Test
    fun invalidFilesAreRejected() {
        assertFailsWith<LayoutFormatException> { LayoutCodec.decode("not json") }
        assertFailsWith<LayoutFormatException> { LayoutCodec.decode("""{"format":"other","version":1,"layouts":[]}""") }
        assertFailsWith<LayoutFormatException> {
            LayoutCodec.decode("""{"format":"custom-drag-keyboard","version":1,"layouts":[]}""")
        }
        // 키 개수가 rows * columns와 맞지 않음
        assertFailsWith<LayoutFormatException> {
            LayoutCodec.decode(
                """{"format":"custom-drag-keyboard","version":1,"layouts":[
                  {"id":"x","name":"x","language":"KOREAN","rows":2,"columns":2,"keys":[]}]}""",
            )
        }
    }

    @Test
    fun edgeActionsRoundTripAndAreOptional() {
        val layout = DefaultLayouts.english().copy(
            leftEdge = KeyAction.Special(SpecialKey.BACKSPACE),
            rightEdge = KeyAction.Text(" "),
        )
        assertEquals(layout, LayoutCodec.decode(LayoutCodec.encode(listOf(layout))).single())
        assertEquals(layout.leftEdge, layout.resized(2, 2).leftEdge)

        // 여백 동작이 없던 파일도 그대로 읽힌다
        val old = LayoutCodec.decode(
            """{"format":"custom-drag-keyboard","version":1,"layouts":[
              {"id":"x","name":"x","language":"ENGLISH","rows":1,"columns":1,
               "keys":[{"actions":[null,null,null,null,{"type":"text","text":"a"},null,null,null,null]}]}]}""",
        ).single()
        assertEquals(null, old.leftEdge)
        assertEquals(null, old.rightEdge)
    }
}
