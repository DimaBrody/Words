package inc.brody.words.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import inc.brody.words.data.db.entity.WordEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [WordEntry::class],
    version = 1
)
abstract class WordsDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao

    companion object {
        @Volatile
        private var instance: WordsDatabase? = null
        private val LOCK = Any()

        operator fun invoke(
            context: Context,
            scope: CoroutineScope
        ) = instance ?: synchronized(LOCK) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                WordsDatabase::class.java,
                "words.db")
                .addCallback(WordDatabaseCallback(scope)).build().also { instance = it }

        }
    }

    private class WordDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            instance?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.wordsDao())
                }
            }
        }

        private fun populateDatabase(wordsDao: WordsDao) {
            val word = WordEntry("Hello", "World")
            //wordsDao.insert(word)
        }

    }
}