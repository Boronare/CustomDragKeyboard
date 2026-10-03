package com.naremotion.dragkeyboard.core

import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionDetectorTest {
    private fun detect(dx: Float, dy: Float) = DirectionDetector.detect(dx, dy, 10f, 10f)

    @Test
    fun smallMovementIsTap() {
        assertEquals(Direction.TAP, detect(0f, 0f))
        assertEquals(Direction.TAP, detect(6f, -6f))
    }

    @Test
    fun eightDirections() {
        assertEquals(Direction.E, detect(20f, 1f))
        assertEquals(Direction.W, detect(-20f, -1f))
        assertEquals(Direction.N, detect(1f, -20f))
        assertEquals(Direction.S, detect(-1f, 20f))
        assertEquals(Direction.NE, detect(15f, -15f))
        assertEquals(Direction.NW, detect(-15f, -15f))
        assertEquals(Direction.SE, detect(15f, 15f))
        assertEquals(Direction.SW, detect(-15f, 15f))
    }

    @Test
    fun axisSensitivityIsIndependent() {
        // 세로 감도가 둔하면 같은 거리라도 세로 이동은 TAP으로 남는다
        assertEquals(Direction.E, DirectionDetector.detect(15f, 0f, 10f, 30f))
        assertEquals(Direction.TAP, DirectionDetector.detect(0f, 15f, 10f, 30f))
    }

    @Test
    fun gridPositionsMatchOrdinals() {
        Direction.entries.forEach { assertEquals(it, Direction.at(it.gridRow, it.gridCol)) }
    }

    @Test
    fun hybridKeepsShortDragsCardinal() {
        val hybrid = { dx: Float, dy: Float -> DirectionDetector.detect(dx, dy, 10f, 10f, diagonalScale = 2f) }
        // 짧은 대각선 드래그는 더 많이 움직인 축의 상하좌우로 판정한다
        assertEquals(Direction.E, hybrid(12f, -9f))
        assertEquals(Direction.N, hybrid(9f, -12f))
        assertEquals(Direction.TAP, hybrid(6f, 6f))
        // 크게 그어야 대각선이 된다
        assertEquals(Direction.NE, hybrid(16f, -16f))
        assertEquals(Direction.SW, hybrid(-15f, 15f))
        // 멀리 가도 축에 가까우면 상하좌우
        assertEquals(Direction.S, hybrid(5f, 30f))
    }
}
