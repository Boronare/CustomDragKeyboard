package io.github.boronare.dragkeyboard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DragKeyboardTheme { App() }
        }
    }
}

private const val HOME = "home"
private const val PREFS = "prefs"
private const val EDIT_PREFIX = "edit:"

@Composable
private fun App(vm: SettingsViewModel = viewModel()) {
    var route by rememberSaveable { mutableStateOf(HOME) }
    val goHome = { route = HOME }

    when {
        route == PREFS -> {
            BackHandler(onBack = goHome)
            PreferencesScreen(vm.prefs, vm::updatePrefs, onBack = goHome)
        }
        route.startsWith(EDIT_PREFIX) -> {
            val layout = vm.layout(route.removePrefix(EDIT_PREFIX))
            if (layout == null) {
                LaunchedEffect(Unit) { goHome() }
            } else {
                LayoutEditorScreen(
                    original = layout,
                    onSave = { vm.replace(it); goHome() },
                    onBack = goHome,
                )
            }
        }
        else -> HomeScreen(
            vm = vm,
            onEdit = { route = EDIT_PREFIX + it },
            onOpenPrefs = { route = PREFS },
        )
    }
}
