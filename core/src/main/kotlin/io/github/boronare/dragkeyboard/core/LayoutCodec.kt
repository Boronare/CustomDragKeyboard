package io.github.boronare.dragkeyboard.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** 내보내기/불러오기 파일 형식. */
@Serializable
data class LayoutBundle(
    val format: String,
    val version: Int,
    val layouts: List<KeyboardLayout>,
)

class LayoutFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

object LayoutCodec {
    const val FORMAT = "custom-drag-keyboard"
    const val VERSION = 1

    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = true
    }

    fun encode(layouts: List<KeyboardLayout>): String =
        json.encodeToString(LayoutBundle.serializer(), LayoutBundle(FORMAT, VERSION, layouts))

    fun decode(text: String): List<KeyboardLayout> {
        val bundle = try {
            json.decodeFromString(LayoutBundle.serializer(), text)
        } catch (e: SerializationException) {
            throw LayoutFormatException("not a keyboard layout file", e)
        } catch (e: IllegalArgumentException) {
            throw LayoutFormatException("invalid keyboard layout: ${e.message}", e)
        }
        if (bundle.format != FORMAT) throw LayoutFormatException("unknown format: ${bundle.format}")
        if (bundle.version > VERSION) throw LayoutFormatException("unsupported version: ${bundle.version}")
        if (bundle.layouts.isEmpty()) throw LayoutFormatException("file contains no layouts")
        return bundle.layouts
    }
}
