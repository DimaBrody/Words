package inc.brody.words.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import inc.brody.words.data.providers.PreferenceProvider
import inc.brody.words.data.repository.DatabaseRepository

@Suppress("UNCHECKED_CAST")
class WordsViewModelFactory(
    private val databaseRepository: DatabaseRepository,
    private val prefsProvider: PreferenceProvider
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WordsViewModel(databaseRepository,prefsProvider) as T
    }

}