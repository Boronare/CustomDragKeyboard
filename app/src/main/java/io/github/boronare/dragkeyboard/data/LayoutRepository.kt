package io.github.boronare.dragkeyboard.data

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import io.github.boronare.dragkeyboard.core.DefaultLayouts
import io.github.boronare.dragkeyboard.core.KeyboardLayout
import io.github.boronare.dragkeyboard.core.LayoutCodec
import java.io.File

/** 사용자의 자판 목록을 앱 내부 저장소에 JSON으로 보관한다. */
class LayoutRepository(context: Context) {
    private val file = AtomicFile(File(context.applicationContext.filesDir, "layouts.json"))

    /** 저장된 파일의 변경 시각. 키보드 서비스가 다시 읽을지 판단하는 데 쓴다. */
    val lastModified: Long get() = file.baseFile.lastModified()

    fun load(): List<KeyboardLayout> {
        if (!file.baseFile.exists()) return DefaultLayouts.all()
        return try {
            LayoutCodec.decode(file.readFully().decodeToString())
        } catch (e: Exception) {
            Log.w(TAG, "saved layouts are unreadable, falling back to defaults", e)
            DefaultLayouts.all()
        }
    }

    fun save(layouts: List<KeyboardLayout>) {
        val out = file.startWrite()
        try {
            out.write(LayoutCodec.encode(layouts).encodeToByteArray())
            file.finishWrite(out)
        } catch (e: Exception) {
            file.failWrite(out)
            throw e
        }
    }

    private companion object {
        const val TAG = "LayoutRepository"
    }
}
