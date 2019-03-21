package inc.brody.words.data.utils

import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import inc.brody.words.data.db.entity.WordEntry
import inc.brody.words.data.providers.PreferenceProvider
import inc.brody.words.internal.liveData
import java.lang.NullPointerException

object FirebaseUtil {

    val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }

    val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }

    val uid: String?
        get() = auth.currentUser?.uid

    val currentUserDocRef: DocumentReference
        get() = firestore.collection("Users").document(uid ?: throw NullPointerException("UID is null"))

    val currentWordsDocRef: CollectionReference
        get() = firestore.collection("Words")

    val liveDataList: LiveData<List<WordEntry>>
        get() = firestore.collection("Words").liveData(clazz = WordEntry::class.java) {
                it["uid"] == uid
            }

    fun setCurrentWord(word: WordEntry,setWord: (Task<Void>)->Unit){
        currentWordsDocRef.document(word.word)
            .set(word).addOnCompleteListener {
                setWord(it)
            }
    }

    fun deleteCurrentWord(word: WordEntry,deleteWord: (Task<Void>)->Unit){
        currentWordsDocRef.document(word.word)
            .delete().addOnCompleteListener {
                deleteWord(it)
            }
    }

}