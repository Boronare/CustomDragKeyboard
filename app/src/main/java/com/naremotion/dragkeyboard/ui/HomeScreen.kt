package com.naremotion.dragkeyboard.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naremotion.dragkeyboard.R
import com.naremotion.dragkeyboard.core.DefaultLayouts
import com.naremotion.dragkeyboard.core.KeyboardLayout
import com.naremotion.dragkeyboard.core.Language
import com.naremotion.dragkeyboard.core.LayoutFormatException
import com.naremotion.dragkeyboard.ime.DragKeyboardService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(vm: SettingsViewModel, onEdit: (String) -> Unit, onOpenPrefs: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun toast(message: String) = scope.launch { snackbar.showSnackbar(message) }

    var showNewDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<KeyboardLayout?>(null) }
    // 내보낼 자판 id. 빈 문자열은 전체.
    var exportId by rememberSaveable { mutableStateOf("") }

    val exportedMsg = stringResource(R.string.export_done)
    val failedMsg = stringResource(R.string.file_error)
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val json = vm.exportJson(exportId.ifEmpty { null })
        val ok = runCatching {
            context.contentResolver.openOutputStream(uri, "wt")!!.use { it.write(json.encodeToByteArray()) }
        }.isSuccess
        toast(if (ok) exportedMsg else failedMsg)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val message = try {
            val text = context.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() }
            context.getString(R.string.import_done, vm.importJson(text))
        } catch (e: LayoutFormatException) {
            context.getString(R.string.import_invalid)
        } catch (e: Exception) {
            failedMsg
        }
        toast(message)
    }
    fun export(layout: KeyboardLayout?) {
        exportId = layout?.id ?: ""
        exportLauncher.launch((layout?.name ?: "layouts").replace(Regex("[\\\\/:*?\"<>|]"), "_") + ".json")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenPrefs) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.preferences))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().consumeWindowInsets(padding).imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SetupCard() }
            item {
                var sample by rememberSaveable { mutableStateOf("") }
                OutlinedTextField(
                    value = sample,
                    onValueChange = { sample = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.try_it)) },
                    placeholder = { Text(stringResource(R.string.try_it_hint)) },
                )
            }
            item {
                Text(
                    stringResource(R.string.layouts),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(onClick = { showNewDialog = true }) { Text(stringResource(R.string.new_layout)) }
                    OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }) {
                        Text(stringResource(R.string.import_layout))
                    }
                    OutlinedButton(onClick = { export(null) }) { Text(stringResource(R.string.export_all)) }
                }
            }
            itemsIndexed(vm.layouts, key = { _, l -> l.id }) { index, layout ->
                LayoutRow(
                    layout = layout,
                    canMoveUp = index > 0,
                    canMoveDown = index < vm.layouts.lastIndex,
                    canDelete = vm.layouts.size > 1,
                    onEdit = { onEdit(layout.id) },
                    onMove = { vm.move(layout.id, it) },
                    onDuplicate = { vm.duplicate(layout.id, context.getString(R.string.copy_suffix)) },
                    onExport = { export(layout) },
                    onDelete = { deleteTarget = layout },
                )
            }
            item {
                Text(
                    stringResource(R.string.layouts_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showNewDialog) {
        NewLayoutDialog(
            onDismiss = { showNewDialog = false },
            onCreate = {
                showNewDialog = false
                vm.add(it)
                onEdit(it.id)
            },
        )
    }
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_message, target.name)) },
            confirmButton = {
                TextButton(onClick = { vm.delete(target.id); deleteTarget = null }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

private data class ImeStatus(val enabled: Boolean, val selected: Boolean)

private fun imeStatus(context: Context): ImeStatus {
    val me = ComponentName(context, DragKeyboardService::class.java)
    val imm = context.getSystemService(InputMethodManager::class.java)
    val enabled = imm?.enabledInputMethodList.orEmpty().any { it.component == me }
    val current = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
    return ImeStatus(enabled, current?.let(ComponentName::unflattenFromString) == me)
}

@Composable
private fun SetupCard() {
    val context = LocalContext.current
    var status by remember { mutableStateOf(imeStatus(context)) }
    // 설정 화면이나 입력기 선택 창에서 돌아오는 시점을 알기 어려워 주기적으로 확인한다
    LaunchedEffect(Unit) {
        while (true) {
            status = imeStatus(context)
            delay(1000)
        }
    }
    if (status.enabled && status.selected) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.titleMedium)
            SetupStep(
                number = 1,
                text = stringResource(R.string.setup_enable),
                done = status.enabled,
                button = stringResource(R.string.setup_enable_button),
                onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
            )
            SetupStep(
                number = 2,
                text = stringResource(R.string.setup_select),
                done = status.selected,
                enabled = status.enabled,
                button = stringResource(R.string.setup_select_button),
                onClick = { context.getSystemService(InputMethodManager::class.java)?.showInputMethodPicker() },
            )
        }
    }
}

@Composable
private fun SetupStep(number: Int, text: String, done: Boolean, button: String, onClick: () -> Unit, enabled: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (done) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        } else {
            Text("$number.", style = MaterialTheme.typography.titleMedium)
        }
        Text(text, modifier = Modifier.weight(1f))
        if (!done) FilledTonalButton(onClick = onClick, enabled = enabled) { Text(button) }
    }
}

@Composable
private fun LayoutRow(
    layout: KeyboardLayout,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onMove: (Int) -> Unit,
    onDuplicate: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)) {
        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(layout.name, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.layout_summary, languageName(layout.language), layout.columns, layout.rows),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onMove(-1) }, enabled = canMoveUp) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.move_up))
            }
            IconButton(onClick = { onMove(1) }, enabled = canMoveDown) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.move_down))
            }
            var menu by remember { mutableStateOf(false) }
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, onClick = { menu = false; onEdit() })
                DropdownMenuItem(text = { Text(stringResource(R.string.duplicate)) }, onClick = { menu = false; onDuplicate() })
                DropdownMenuItem(text = { Text(stringResource(R.string.export)) }, onClick = { menu = false; onExport() })
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete)) },
                    enabled = canDelete,
                    onClick = { menu = false; onDelete() },
                )
            }
        }
    }
}

@Composable
fun languageName(language: Language): String = stringResource(
    when (language) {
        Language.KOREAN -> R.string.language_korean
        Language.ENGLISH -> R.string.language_english
    },
)

private enum class Template { BLANK, CLASSIC, ENGLISH }

@Composable
private fun NewLayoutDialog(onDismiss: () -> Unit, onCreate: (KeyboardLayout) -> Unit) {
    var template by remember { mutableStateOf(Template.BLANK) }
    var language by remember { mutableStateOf(Language.KOREAN) }
    val blankName = stringResource(R.string.new_layout_default_name)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_layout)) },
        text = {
            Column {
                Template.entries.forEach { t ->
                    Row(
                        Modifier.fillMaxWidth().clickable { template = t },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = template == t, onClick = { template = t })
                        Text(
                            stringResource(
                                when (t) {
                                    Template.BLANK -> R.string.template_blank
                                    Template.CLASSIC -> R.string.template_classic
                                    Template.ENGLISH -> R.string.template_english
                                },
                            ),
                        )
                    }
                }
                if (template == Template.BLANK) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 12.dp)) {
                        Language.entries.forEach { lang ->
                            FilterChip(
                                selected = language == lang,
                                onClick = { language = lang },
                                label = { Text(languageName(lang)) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val layout = when (template) {
                    Template.BLANK -> KeyboardLayout.blank(blankName, language, 3, 5)
                    Template.CLASSIC -> DefaultLayouts.classic().copy(id = KeyboardLayout.newId())
                    Template.ENGLISH -> DefaultLayouts.english().copy(id = KeyboardLayout.newId())
                }
                onCreate(layout)
            }) { Text(stringResource(R.string.create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
