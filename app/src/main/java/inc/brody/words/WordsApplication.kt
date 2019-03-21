package inc.brody.words

import android.app.Application
import android.preference.PreferenceManager
import inc.brody.words.data.WordsDatabase
import inc.brody.words.data.providers.PreferenceProvider
import inc.brody.words.data.repository.DatabaseRepository
import inc.brody.words.data.repository.DatabaseRepositoryImpl
import inc.brody.words.ui.words.WordsViewModel
import inc.brody.words.ui.words.WordsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class WordsApplication : Application(),KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@WordsApplication))

        bind() from singleton { WordsDatabase(instance(), CoroutineScope(Job() + Dispatchers.Main)) }
        bind() from singleton { instance<WordsDatabase>().wordsDao() }
        bind() from singleton { PreferenceProvider(instance()) }
        bind<DatabaseRepository>() with singleton { DatabaseRepositoryImpl(instance()) }
        bind() from provider { WordsViewModelFactory(instance(),instance()) }
        bind() from provider { WordsViewModel(instance(),instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        PreferenceManager.setDefaultValues(this,R.xml.preferences,false)
    }

}