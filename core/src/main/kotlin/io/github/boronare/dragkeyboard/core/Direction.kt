package io.github.boronare.dragkeyboard.core

import kotlin.math.PI
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
     * 이동량을 축별 감도([thresholdX], [thresholdY])로 나눈 타원 안쪽이면 TAP,
     * 바깥이면 8방향 중 가장 가까운 방향이다.
     */
    fun detect(dx: Float, dy: Float, thresholdX: Float, thresholdY: Float): Direction {
        require(thresholdX > 0f && thresholdY > 0f) { "threshold must be positive" }
        val nx = dx / thresholdX
        val ny = dy / thresholdY
        if (nx * nx + ny * ny < 1f) return Direction.TAP
        val sector = (atan2(ny.toDouble(), nx.toDouble()) / (PI / 4)).roundToInt()
        return SECTORS[Math.floorMod(sector, 8)]
    }
}
