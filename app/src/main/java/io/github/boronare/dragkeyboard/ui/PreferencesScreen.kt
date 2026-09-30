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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
fun PreferencesScreen(prefs: KeyboardPrefs, onChange: (KeyboardPrefs) -> Unit, onBack: () -> Unit) {
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
        }
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
