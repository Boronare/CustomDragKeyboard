package com.naremotion.dragkeyboard.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * 키 하나가 갖는 9개의 입력 방향.
 * 순서는 키 위의 3x3 칸을 왼쪽 위부터 읽은 순서이며, 가운데(TAP)가 단순 터치다.
 */
enum class Direction(val gridRow: Int, val gridCol: Int) {
    NW(0, 0), N(0, 1), NE(0, 2),
    W(1, 0), TAP(1, 1), E(1, 2),
    SW(2, 0), S(2, 1), SE(2, 2);

    companion object {
        fun at(gridRow: Int, gridCol: Int): Direction = entries[gridRow * 3 + gridCol]
    }
}

object DirectionDetector {
    // atan2 결과를 45도 단위로 나눈 구간. 화면 좌표계라 y축이 아래로 증가한다.
    private val SECTORS = arrayOf(
        Direction.E, Direction.SE, Direction.S, Direction.SW,
        Direction.W, Direction.NW, Direction.N, Direction.NE,
    )

    /**
     * 터치 시작점으로부터의 이동량([dx], [dy])으로 방향을 판정한다.
     * 이동량을 축별 감도([thresholdX], [thresholdY])로 나눈 거리 r에 따라
     * - r < 1: TAP
     * - 1 <= r < [diagonalScale]: 상하좌우만 (십자 판정). 짧은 드래그가 대각선으로 새지 않는다.
     * - r >= [diagonalScale]: 45도씩 8방향 (각도 판정). 대각선은 크게 그어야 나온다.
     * [diagonalScale]이 1이면 처음부터 8방향 각도 판정이다.
     */
    fun detect(dx: Float, dy: Float, thresholdX: Float, thresholdY: Float, diagonalScale: Float = 1f): Direction {
        require(thresholdX > 0f && thresholdY > 0f) { "threshold must be positive" }
        val nx = dx / thresholdX
        val ny = dy / thresholdY
        val r2 = nx * nx + ny * ny
        if (r2 < 1f) return Direction.TAP
        if (r2 < diagonalScale * diagonalScale) {
            return if (abs(nx) >= abs(ny)) {
                if (nx > 0) Direction.E else Direction.W
            } else {
                if (ny > 0) Direction.S else Direction.N
            }
        }
        val sector = (atan2(ny.toDouble(), nx.toDouble()) / (PI / 4)).roundToInt()
        return SECTORS[Math.floorMod(sector, 8)]
    }
}
