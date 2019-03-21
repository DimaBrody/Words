package inc.brody.words.ui.words

import androidx.lifecycle.ViewModel
import inc.brody.words.data.db.entity.WordEntry
import inc.brody.words.data.providers.PreferenceProvider
import inc.brody.words.data.repository.DatabaseRepository
import inc.brody.words.data.utils.FirebaseUtil
import inc.brody.words.internal.*
import kotlinx.coroutines.*

class WordsViewModel(
    private val databaseRepository: DatabaseRepository,
    val prefsProvider: PreferenceProvider
) : ViewModel() {

    fun addWord(word: WordEntry) {
        if (prefsProvider.isParticularSyncNeeded) {
            FirebaseUtil.setCurrentWord(word) {
                if (!it.isSuccessful)
                    prefsProvider.isSyncNeeded = true

                addScopedWord(word)
            }
        } else prefsProvider.setSyncConditions {
            addScopedWord(word)
        }

    }

    fun deleteWord(word: WordEntry) {
        if (prefsProvider.isParticularSyncNeeded) {
            FirebaseUtil.deleteCurrentWord(word) {
                if (!it.isSuccessful)
                    prefsProvider.setSyncConditions {
                        deleteScopedWord(word.copy(isAlive = false), false)
                    }
                else
                    deleteScopedWord(word)
            }
        } else prefsProvider.setSyncConditions {
            deleteScopedWord(word.copy(isAlive = false), false)
        }
    }


    fun commitSynchronizationAsync(words: List<WordEntry>?) = GlobalScope.async {
        if (prefsProvider.isFullSyncNeeded) {
            val batch = FirebaseUtil.firestore.batch()

            words?.forEach {
                when {
                    !it.isAlive -> batch.deleteWord(it.word)
                    it.uid == prefsProvider.currentSyncUid -> batch.setWord(it)
                    it.uid == null ->
                        batch.setWord(it.copy(uid = prefsProvider.currentSyncUid))
                }
            }

            batch.commitWords {
                prefsProvider.isSyncNeeded = false
                words?.forEach { word ->
                    if (!word.isAlive || (word.uid != null && word.uid != FirebaseUtil.uid))
                        deleteScopedWord(word)
                }
            }

            words

        } else words
    }


    fun addListOfWords(words: List<WordEntry>) = GlobalScope.launch {
        databaseRepository.addListOfWords(words)
    }

    private fun addScopedWord(word: WordEntry) = GlobalScope.launch {
        databaseRepository.addWord(word)
    }

    private fun deleteScopedWord(word: WordEntry, isDelete: Boolean = true) = GlobalScope.launch {
        databaseRepository.deleteWord(word, isDelete)
    }

    val words by lazyDeferred {
        databaseRepository.getListOfWords()
    }

    val firestoreWords
        get() = GlobalScope.async {
            databaseRepository.getListFromFirebase()
        }


}