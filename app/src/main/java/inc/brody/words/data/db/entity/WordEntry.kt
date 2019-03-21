package inc.brody.words.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "words_database")
data class WordEntry(
    @PrimaryKey(autoGenerate = false)
    val word: String = "",
    val desc: String = "",
    val likes: Int = 0,
    val uid: String? = null,
    @Exclude
    val isAlive: Boolean = true
)