package com.naremotion.dragkeyboard.data

import android.content.Context
import android.util.AtomicFile
import android.util.Log
import com.naremotion.dragkeyboard.core.Language
import com.naremotion.dragkeyboard.core.UserHistory
import com.naremotion.dragkeyboard.core.WordDictionary
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * 단어 추천 데이터. 기본 사전(assets/dict)은 처음 필요할 때 백그라운드에서 읽어 메모리에 두고,
 * 사용자가 입력한 단어는 앱 내부 저장소에 따로 보관한다.
 */
class WordStore(context: Context) {
    private val app = context.applicationContext
    private val historyFile = AtomicFile(File(app.filesDir, "user_words.tsv"))
    private val dictionaries = ConcurrentHashMap<Language, WordDictionary>()
    private val loading = ConcurrentHashMap.newKeySet<Language>()
    private val loader = Executors.newSingleThreadExecutor()

    /** 불러오기가 끝나지 않았으면 null을 돌려주고 백그라운드에서 읽기 시작한다. */
    fun dictionary(language: Language): WordDictionary? {
        dictionaries[language]?.let { return it }
        preload(language)
        return null
    }

    fun preload(language: Language) {
        if (dictionaries.containsKey(language) || !loading.add(language)) return
        loader.execute {
            try {
                val name = if (language == Language.KOREAN) "ko" else "en"
                app.assets.open("dict/$name.tsv").bufferedReader().useLines { lines ->
                    dictionaries[language] = WordDictionary.parse(lines)
                }
            } catch (e: Exception) {
                Log.w(TAG, "could not load $language dictionary", e)
            } finally {
                loading.remove(language)
            }
        }
    }

    /** 학습 파일의 변경 시각. 설정 앱에서 지웠는지 키보드가 알아차리는 데 쓴다. */
    val historyStamp: Long get() = historyFile.baseFile.lastModified()

    fun loadHistory(): UserHistory = try {
        if (historyFile.baseFile.exists()) UserHistory.decode(historyFile.readFully().decodeToString()) else UserHistory()
    } catch (e: Exception) {
        Log.w(TAG, "learned words are unreadable", e)
        UserHistory()
    }

    fun saveHistory(history: UserHistory) {
        write(history.encode())
        history.markSaved()
    }

    /** 빈 파일을 써서 변경 시각을 바꾼다. 키보드가 들고 있던 기록을 다시 저장하지 않고 버리게 된다. */
    fun clearHistory() = write("")

    private fun write(text: String) {
        val out = historyFile.startWrite()
        try {
            out.write(text.encodeToByteArray())
            historyFile.finishWrite(out)
        } catch (e: Exception) {
            historyFile.failWrite(out)
            Log.w(TAG, "could not save learned words", e)
        }
    }

    private companion object {
        const val TAG = "WordStore"
    }
}
