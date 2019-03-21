package inc.brody.words.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import inc.brody.words.data.db.entity.WordEntry

@Dao
interface WordsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wordEntry: WordEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(words: List<WordEntry>)

    @Query("select * from words_database")
    fun getWordsList() : LiveData<List<WordEntry>>

    @Query("delete from words_database where word == :word")
    fun deleteWord(word: String)

    @Query("delete from words_database")
    fun deleteAll()

}