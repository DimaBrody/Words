package inc.brody.words.data.db.livedata

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class QueryLiveData<T>(
    private val query: Query,
    private val clazz: Class<T>? = null,
    private val parser: ((documentSnapshot: DocumentSnapshot) -> Boolean)? = null
) : LiveData<List<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                when {
                    parser != null -> {
                        postValue(querySnapshot?.documents?.filter { parser.invoke(it) }?.map { it.toObject(clazz!!)!! })
                    }
                    else -> value = querySnapshot?.documents as List<T>?
                }
            } else
                Log.d("FirestoreData", "", firebaseFirestoreException)
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }

}