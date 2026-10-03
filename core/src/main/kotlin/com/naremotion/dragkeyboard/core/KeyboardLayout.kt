package com.naremotion.dragkeyboard.core

import kotlinx.serialization.Serializable
import java.util.UUID

enum class Language { ENGLISH, KOREAN }

/** 키 하나. [actions]는 [Direction] 순서대로 9칸이며 null은 비어 있는 방향이다. */
@Serializable
data class KeySpec(
    val actions: List<KeyAction?> = List(Direction.entries.size) { null },
    /** 길게 누르고 있으면 TAP 동작을 반복한다 (백스페이스 등). */
    val repeat: Boolean = false,
) {
    init {
        require(actions.size == Direction.entries.size) { "a key needs exactly 9 directions" }
    }

    operator fun get(direction: Direction): KeyAction? = actions[direction.ordinal]

    fun with(direction: Direction, action: KeyAction?): KeySpec =
        copy(actions = actions.toMutableList().also { it[direction.ordinal] = action })

    val isEmpty: Boolean get() = actions.all { it == null }

    companion object {
        val EMPTY = KeySpec()
    }
}

@Serializable
data class KeyboardLayout(
    val id: String,
    val name: String,
    val language: Language,
    val rows: Int,
    val columns: Int,
    /** 행 우선 순서로 rows * columns 개. */
    val keys: List<KeySpec>,
    /** 왼쪽 여백을 눌렀을 때의 원터치 동작. null이면 여백은 드래그 공간으로만 쓴다. */
    val leftEdge: KeyAction? = null,
    /** 오른쪽 여백을 눌렀을 때의 원터치 동작. */
    val rightEdge: KeyAction? = null,
) {
    init {
        require(rows in ROW_RANGE) { "rows must be in $ROW_RANGE" }
        require(columns in COLUMN_RANGE) { "columns must be in $COLUMN_RANGE" }
        require(keys.size == rows * columns) { "expected ${rows * columns} keys but got ${keys.size}" }
    }

    fun key(row: Int, column: Int): KeySpec = keys[row * columns + column]

    fun withKey(row: Int, column: Int, key: KeySpec): KeyboardLayout =
        copy(keys = keys.toMutableList().also { it[row * columns + column] = key })

    /** 크기를 바꾼다. 겹치는 위치의 키는 그대로 남고 새로 생긴 칸은 비어 있다. */
    fun resized(newRows: Int, newColumns: Int): KeyboardLayout = copy(
        rows = newRows,
        columns = newColumns,
        keys = List(newRows * newColumns) { i ->
            val r = i / newColumns
            val c = i % newColumns
            if (r < rows && c < columns) key(r, c) else KeySpec.EMPTY
        },
    )

    companion object {
        val ROW_RANGE = 1..6
        val COLUMN_RANGE = 1..10

        fun newId(): String = UUID.randomUUID().toString()

        fun blank(name: String, language: Language, rows: Int, columns: Int) = KeyboardLayout(
            id = newId(),
            name = name,
            language = language,
            rows = rows,
            columns = columns,
            keys = List(rows * columns) { KeySpec.EMPTY },
        )
    }
}
