package io.github.boronare.dragkeyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.boronare.dragkeyboard.R
import io.github.boronare.dragkeyboard.data.KeyboardPrefs
import io.github.boronare.dragkeyboard.data.Margins
import io.github.boronare.dragkeyboard.data.Vibration
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    prefs: KeyboardPrefs,
    onChange: (KeyboardPrefs) -> Unit,
    onClearLearnedWords: () -> Unit,
    onBack: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    var cleared by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.preferences)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Section(stringResource(R.string.pref_vibration))
            Vibration.entries.forEach { v ->
                RadioRow(
                    stringResource(
                        when (v) {
                            Vibration.OFF -> R.string.vibration_off
                            Vibration.LIGHT -> R.string.vibration_light
                            Vibration.STRONG -> R.string.vibration_strong
                        },
                    ),
                    selected = prefs.vibration == v,
                ) { onChange(prefs.copy(vibration = v)) }
            }

            Section(stringResource(R.string.pref_sensitivity), stringResource(R.string.pref_sensitivity_hint))
            MmSlider(stringResource(R.string.pref_sensitivity_x), prefs.sensitivityXMm, 1f..8f) {
                onChange(prefs.copy(sensitivityXMm = it))
            }
            MmSlider(stringResource(R.string.pref_sensitivity_y), prefs.sensitivityYMm, 1f..8f) {
                onChange(prefs.copy(sensitivityYMm = it))
            }
            ValueSlider(
                label = stringResource(R.string.pref_diagonal),
                value = prefs.diagonalScale,
                range = 1f..3f,
                step = 0.1f,
                format = { stringResource(R.string.pref_diagonal_value, it) },
            ) { onChange(prefs.copy(diagonalScale = it)) }
            Text(
                stringResource(R.string.pref_diagonal_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Section(stringResource(R.string.pref_suggestions))
            SwitchRow(stringResource(R.string.pref_suggestions_enable), prefs.suggestions) {
                onChange(prefs.copy(suggestions = it))
            }
            SwitchRow(
                stringResource(R.string.pref_learn_words),
                prefs.learnWords,
                enabled = prefs.suggestions,
                summary = stringResource(R.string.pref_learn_words_hint),
            ) { onChange(prefs.copy(learnWords = it)) }
            OutlinedButton(onClick = { confirmClear = true }, enabled = !cleared) {
                Text(stringResource(if (cleared) R.string.pref_clear_learned_done else R.string.pref_clear_learned))
            }

            Section(stringResource(R.string.pref_key_height))
            MmSlider(stringResource(R.string.pref_key_height_value), prefs.keyHeightMm, 7f..18f) {
                onChange(prefs.copy(keyHeightMm = it))
            }

            Section(stringResource(R.string.pref_margins_portrait), stringResource(R.string.pref_margins_hint))
            MarginSliders(prefs.portraitMargins) { onChange(prefs.copy(portraitMargins = it)) }
            Section(stringResource(R.string.pref_margins_landscape))
            MarginSliders(prefs.landscapeMargins) { onChange(prefs.copy(landscapeMargins = it)) }

            Section(stringResource(R.string.about))
            val context = LocalContext.current
            val version = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrNull().orEmpty()
            Text(stringResource(R.string.about_text, version), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.about_dictionary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.pref_clear_learned)) },
            text = { Text(stringResource(R.string.pref_clear_learned_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onClearLearnedWords()
                    cleared = true
                    confirmClear = false
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    enabled: Boolean = true,
    summary: String? = null,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().toggleable(value = checked, enabled = enabled, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = if (enabled) Color.Unspecified else MaterialTheme.colorScheme.outline)
            if (summary != null) {
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
private fun ValueSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    format: @Composable (Float) -> String,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row {
            Text(label, modifier = Modifier.weight(1f))
            Text(format(value), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = value.coerceIn(range),
            onValueChange = { onChange((it / step).roundToInt() * step) },
            valueRange = range,
            steps = ((range.endInclusive - range.start) / step).roundToInt() - 1,
        )
    }
}

@Composable
private fun Section(title: String, summary: String? = null) {
    HorizontalDivider(Modifier.padding(top = 8.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    if (summary != null) {
        Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MarginSliders(margins: Margins, onChange: (Margins) -> Unit) {
    MmSlider(stringResource(R.string.margin_left), margins.leftMm, 0f..15f) { onChange(margins.copy(leftMm = it)) }
    MmSlider(stringResource(R.string.margin_right), margins.rightMm, 0f..15f) { onChange(margins.copy(rightMm = it)) }
    MmSlider(stringResource(R.string.margin_bottom), margins.bottomMm, 0f..15f) { onChange(margins.copy(bottomMm = it)) }
}

/** 0.5mm 단위로 움직이는 슬라이더. */
@Composable
private fun MmSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row {
            Text(label, modifier = Modifier.weight(1f))
            Text(String.format(Locale.ROOT, "%.1f mm", value), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = value.coerceIn(range),
            onValueChange = { onChange((it * 2).roundToInt() / 2f) },
            valueRange = range,
            steps = ((range.endInclusive - range.start) * 2).roundToInt() - 1,
        )
    }
}
