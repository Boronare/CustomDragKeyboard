package io.github.boronare.dragkeyboard.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import io.github.boronare.dragkeyboard.core.KeyboardLayout
import io.github.boronare.dragkeyboard.core.LayoutCodec
import io.github.boronare.dragkeyboard.data.KeyboardPrefs
import io.github.boronare.dragkeyboard.data.LayoutRepository
import io.github.boronare.dragkeyboard.data.PrefsStore

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = LayoutRepository(app)
    private val prefsStore = PrefsStore(app)

    var layouts by mutableStateOf(repository.load())
        private set
    var prefs by mutableStateOf(prefsStore.load())
        private set

    private fun commit(newLayouts: List<KeyboardLayout>) {
        layouts = newLayouts
        repository.save(newLayouts)
    }

    fun updatePrefs(newPrefs: KeyboardPrefs) {
        prefs = newPrefs
        prefsStore.save(newPrefs)
    }

    fun layout(id: String): KeyboardLayout? = layouts.find { it.id == id }

    fun replace(layout: KeyboardLayout) = commit(layouts.map { if (it.id == layout.id) layout else it })

    fun add(layout: KeyboardLayout) = commit(layouts + layout)

    fun duplicate(id: String, nameSuffix: String) {
        val index = layouts.indexOfFirst { it.id == id }
        if (index < 0) return
        val source = layouts[index]
        val copy = source.copy(id = KeyboardLayout.newId(), name = source.name + nameSuffix)
        commit(layouts.toMutableList().apply { add(index + 1, copy) })
    }

    /** 자판이 최소 하나는 남아 있어야 키보드가 동작한다. */
    fun delete(id: String) {
        if (layouts.size > 1) commit(layouts.filterNot { it.id == id })
    }

    fun move(id: String, delta: Int) {
        val from = layouts.indexOfFirst { it.id == id }
        val to = from + delta
        if (from < 0 || to !in layouts.indices) return
        commit(layouts.toMutableList().apply { add(to, removeAt(from)) })
    }

    /** [id]가 null이면 모든 자판을 내보낸다. */
    fun exportJson(id: String?): String =
        LayoutCodec.encode(if (id == null) layouts else layouts.filter { it.id == id })

    /** @throws io.github.boronare.dragkeyboard.core.LayoutFormatException 형식이 맞지 않을 때 */
    fun importJson(text: String): Int {
        val imported = LayoutCodec.decode(text).map { it.copy(id = KeyboardLayout.newId()) }
        commit(layouts + imported)
        return imported.size
    }
}
