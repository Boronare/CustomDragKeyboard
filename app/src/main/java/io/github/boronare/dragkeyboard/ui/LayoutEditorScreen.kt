package io.github.boronare.dragkeyboard.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.boronare.dragkeyboard.R
import io.github.boronare.dragkeyboard.core.Direction
import io.github.boronare.dragkeyboard.core.KeySpec
import io.github.boronare.dragkeyboard.core.KeyboardLayout
import io.github.boronare.dragkeyboard.core.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutEditorScreen(original: KeyboardLayout, onSave: (KeyboardLayout) -> Unit, onBack: () -> Unit) {
    var draft by remember(original.id) { mutableStateOf(original) }
    var editingKey by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var editingEdge by remember { mutableStateOf<Edge?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val tryBack: () -> Unit = {
        if (draft != original) {
            confirmDiscard = true
        } else {
            onBack()
        }
    }
    BackHandler(onBack = tryBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_layout)) },
                navigationIcon = {
                    IconButton(onClick = tryBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onSave(draft.copy(name = draft.name.trim())) },
                        enabled = draft.name.isNotBlank(),
                    ) { Text(stringResource(R.string.save)) }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = draft.name,
                onValueChange = { draft = draft.copy(name = it) },
                label = { Text(stringResource(R.string.layout_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Column {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Language.entries.forEach { lang ->
                        FilterChip(
                            selected = draft.language == lang,
                            onClick = { draft = draft.copy(language = lang) },
                            label = { Text(languageName(lang)) },
                        )
                    }
                }
                Text(
                    stringResource(
                        if (draft.language == Language.KOREAN) R.string.language_korean_hint else R.string.language_english_hint,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Stepper(stringResource(R.string.columns), draft.columns, KeyboardLayout.COLUMN_RANGE) {
                    draft = draft.resized(draft.rows, it)
                }
                Stepper(stringResource(R.string.rows), draft.rows, KeyboardLayout.ROW_RANGE) {
                    draft = draft.resized(it, draft.columns)
                }
            }
            Text(
                stringResource(R.string.edit_layout_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LayoutGrid(draft) { r, c -> editingKey = r to c }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.edge_buttons), style = MaterialTheme.typography.labelLarge)
                Text(
                    stringResource(R.string.edge_buttons_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EdgeCell(stringResource(R.string.edge_left), draft.leftEdge?.displayLabel, Modifier.weight(1f)) {
                        editingEdge = Edge.LEFT
                    }
                    EdgeCell(stringResource(R.string.edge_right), draft.rightEdge?.displayLabel, Modifier.weight(1f)) {
                        editingEdge = Edge.RIGHT
                    }
                }
            }
        }
    }

    editingEdge?.let { edge ->
        ActionEditorDialog(
            title = stringResource(if (edge == Edge.LEFT) R.string.edge_left else R.string.edge_right),
            action = if (edge == Edge.LEFT) draft.leftEdge else draft.rightEdge,
            onDismiss = { editingEdge = null },
            onConfirm = {
                draft = if (edge == Edge.LEFT) draft.copy(leftEdge = it) else draft.copy(rightEdge = it)
                editingEdge = null
            },
        )
    }

    editingKey?.let { (r, c) ->
        KeyEditorDialog(
            key = draft.key(r, c),
            onDismiss = { editingKey = null },
            onConfirm = {
                draft = draft.withKey(r, c, it)
                editingKey = null
            },
        )
    }
    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_message)) },
            confirmButton = { TextButton(onClick = onBack) { Text(stringResource(R.string.discard)) } },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

private enum class Edge { LEFT, RIGHT }

@Composable
private fun EdgeCell(title: String, label: String?, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            label ?: stringResource(R.string.edge_empty),
            fontWeight = if (label != null) FontWeight.Bold else FontWeight.Normal,
            color = if (label != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Stepper(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(value - 1) }, enabled = value > range.first) {
                Text("−", style = MaterialTheme.typography.titleLarge)
            }
            Text("$value", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onChange(value + 1) }, enabled = value < range.last) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun LayoutGrid(layout: KeyboardLayout, onKeyClick: (Int, Int) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(4.dp)) {
            for (r in 0 until layout.rows) {
                Row {
                    for (c in 0 until layout.columns) {
                        KeyPreview(
                            key = layout.key(r, c),
                            modifier = Modifier.weight(1f).height(68.dp).padding(2.dp).clickable { onKeyClick(r, c) },
                        )
                    }
                }
            }
        }
    }
}

/** 키 하나의 9방향 글자를 3x3으로 보여준다. */
@Composable
fun KeyPreview(key: KeySpec, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surface) {
        Column {
            for (gr in 0..2) {
                Row(Modifier.weight(1f)) {
                    for (gc in 0..2) {
                        val d = Direction.at(gr, gc)
                        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                key[d]?.displayLabel ?: "",
                                fontSize = if (d == Direction.TAP) 14.sp else 9.sp,
                                fontWeight = if (d == Direction.TAP) FontWeight.Bold else FontWeight.Normal,
                                color = if (d == Direction.TAP) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 방향을 나타내는 화살표. */
fun Direction.arrow(): String = when (this) {
    Direction.NW -> "↖"
    Direction.N -> "↑"
    Direction.NE -> "↗"
    Direction.W -> "←"
    Direction.TAP -> "●"
    Direction.E -> "→"
    Direction.SW -> "↙"
    Direction.S -> "↓"
    Direction.SE -> "↘"
}

@Composable
private fun KeyEditorDialog(key: KeySpec, onDismiss: () -> Unit, onConfirm: (KeySpec) -> Unit) {
    var draft by remember { mutableStateOf(key) }
    var editing by remember { mutableStateOf<Direction?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_key)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.edit_key_hint), style = MaterialTheme.typography.bodyMedium)
                Column {
                    for (gr in 0..2) {
                        Row {
                            for (gc in 0..2) {
                                val d = Direction.at(gr, gc)
                                DirectionCell(
                                    direction = d,
                                    label = draft[d]?.displayLabel,
                                    modifier = Modifier.weight(1f).height(64.dp).padding(3.dp),
                                    onClick = { editing = d },
                                )
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().clickable { draft = draft.copy(repeat = !draft.repeat) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = draft.repeat,
                        onCheckedChange = { draft = draft.copy(repeat = it) },
                    )
                    Text(stringResource(R.string.key_repeat))
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(draft) }) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
    editing?.let { d ->
        ActionEditorDialog(
            title = stringResource(R.string.edit_action, d.arrow()),
            action = draft[d],
            onDismiss = { editing = null },
            onConfirm = {
                draft = draft.with(d, it)
                editing = null
            },
        )
    }
}

@Composable
private fun DirectionCell(direction: Direction, label: String?, modifier: Modifier, onClick: () -> Unit) {
    val center = direction == Direction.TAP
    Box(
        modifier
            .border(
                width = if (center) 2.dp else 1.dp,
                color = if (center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            direction.arrow(),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
        )
        Text(
            label ?: "—",
            fontSize = 18.sp,
            fontWeight = if (label != null) FontWeight.Bold else FontWeight.Normal,
            color = if (label != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
