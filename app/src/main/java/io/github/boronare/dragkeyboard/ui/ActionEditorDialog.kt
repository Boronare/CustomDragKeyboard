package io.github.boronare.dragkeyboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.boronare.dragkeyboard.R
import io.github.boronare.dragkeyboard.core.Direction
import io.github.boronare.dragkeyboard.core.KeyAction
import io.github.boronare.dragkeyboard.core.LayoutTarget
import io.github.boronare.dragkeyboard.core.SpecialKey

private enum class ActionKind(val title: Int) {
    NONE(R.string.action_none),
    TEXT(R.string.action_text),
    SPECIAL(R.string.action_special),
    KEYCODE(R.string.action_keycode),
    NEXT_LAYOUT(R.string.action_next_layout),
    PREVIOUS_LAYOUT(R.string.action_previous_layout),
    SWITCH_IME(R.string.action_switch_ime),
}

private fun kindOf(action: KeyAction?): ActionKind = when (action) {
    null -> ActionKind.NONE
    is KeyAction.Text -> ActionKind.TEXT
    is KeyAction.Special -> ActionKind.SPECIAL
    is KeyAction.KeyCode -> ActionKind.KEYCODE
    is KeyAction.SwitchLayout ->
        if (action.target == LayoutTarget.NEXT) ActionKind.NEXT_LAYOUT else ActionKind.PREVIOUS_LAYOUT
    is KeyAction.SwitchInputMethod -> ActionKind.SWITCH_IME
}

@Composable
fun specialKeyName(key: SpecialKey): String = stringResource(
    when (key) {
        SpecialKey.BACKSPACE -> R.string.special_backspace
        SpecialKey.ENTER -> R.string.special_enter
        SpecialKey.TAB -> R.string.special_tab
        SpecialKey.CURSOR_LEFT -> R.string.special_cursor_left
        SpecialKey.CURSOR_RIGHT -> R.string.special_cursor_right
        SpecialKey.FORWARD_DELETE -> R.string.special_forward_delete
    },
)

/** 한 방향의 동작을 고른다. 원작의 "문자 입력 / Keycode 입력 / 이전·다음 키보드"를 이어받았다. */
@Composable
fun ActionEditorDialog(
    direction: Direction,
    action: KeyAction?,
    onDismiss: () -> Unit,
    onConfirm: (KeyAction?) -> Unit,
) {
    var kind by remember { mutableStateOf(kindOf(action)) }
    var text by remember { mutableStateOf((action as? KeyAction.Text)?.text ?: "") }
    var special by remember { mutableStateOf((action as? KeyAction.Special)?.key ?: SpecialKey.BACKSPACE) }
    var keyCode by remember { mutableStateOf((action as? KeyAction.KeyCode)?.code?.toString() ?: "") }
    var label by remember { mutableStateOf(action?.label ?: "") }

    val customLabel = label.takeIf { it.isNotEmpty() }
    val result: KeyAction? = when (kind) {
        ActionKind.NONE -> null
        ActionKind.TEXT -> text.takeIf { it.isNotEmpty() }?.let { KeyAction.Text(it, customLabel) }
        ActionKind.SPECIAL -> KeyAction.Special(special, customLabel)
        ActionKind.KEYCODE -> keyCode.toIntOrNull()?.takeIf { it > 0 }?.let { KeyAction.KeyCode(it, customLabel) }
        ActionKind.NEXT_LAYOUT -> KeyAction.SwitchLayout(LayoutTarget.NEXT, customLabel)
        ActionKind.PREVIOUS_LAYOUT -> KeyAction.SwitchLayout(LayoutTarget.PREVIOUS, customLabel)
        ActionKind.SWITCH_IME -> KeyAction.SwitchInputMethod(customLabel)
    }
    val valid = kind == ActionKind.NONE || result != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_action, direction.arrow())) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Column {
                    ActionKind.entries.forEach { k ->
                        RadioRow(stringResource(k.title), selected = kind == k) { kind = k }
                    }
                }
                if (kind != ActionKind.NONE) HorizontalDivider()
                when (kind) {
                    ActionKind.TEXT -> OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text(stringResource(R.string.action_text_value)) },
                        supportingText = { Text(stringResource(R.string.action_text_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ActionKind.SPECIAL -> Column {
                        SpecialKey.entries.forEach { key ->
                            RadioRow("${key.symbol}  ${specialKeyName(key)}", selected = special == key) { special = key }
                        }
                    }
                    ActionKind.KEYCODE -> OutlinedTextField(
                        value = keyCode,
                        onValueChange = { v -> keyCode = v.filter(Char::isDigit).take(4) },
                        label = { Text(stringResource(R.string.action_keycode_value)) },
                        supportingText = { Text(stringResource(R.string.action_keycode_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    else -> Unit
                }
                if (kind != ActionKind.NONE) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(stringResource(R.string.action_label)) },
                        placeholder = { result?.defaultLabel?.let { Text(it) } },
                        supportingText = { Text(stringResource(R.string.action_label_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(result) }, enabled = valid) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
fun RadioRow(text: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
