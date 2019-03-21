package inc.brody.words.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import inc.brody.words.data.WordsDao
import inc.brody.words.data.db.entity.WordEntry
import inc.brody.words.data.utils.FirebaseUtil
import inc.brody.words.data.utils.GoogleUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseRepositoryImpl(
    private val wordsDao: WordsDao
) : DatabaseRepository {
    override suspend fun getListOfWords(): LiveData<List<WordEntry>> =
        withContext(Dispatchers.IO) {
            return@withContext wordsDao.getWordsList()
        }

    override suspend fun deleteAll() = wordsDao.deleteAll()

    override suspend fun addWord(word: WordEntry) = wordsDao.insert(word)

    override suspend fun deleteWord(word: WordEntry, isDelete: Boolean) {
        if (isDelete || FirebaseUtil.uid == null)
            wordsDao.deleteWord(word.word)
        else
            wordsDao.insert(word)
    }

    override suspend fun addListOfWords(words: List<WordEntry>) =
        wordsDao.insertList(words)

    override suspend fun getListFromFirebase(): LiveData<List<WordEntry>> =
        FirebaseUtil.liveDataList

}