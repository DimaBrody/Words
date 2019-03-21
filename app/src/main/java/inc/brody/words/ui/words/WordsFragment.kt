package inc.brody.words.ui.words


import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import inc.brody.words.R
import inc.brody.words.data.db.entity.WordEntry
import inc.brody.words.data.utils.GoogleUtil
import inc.brody.words.internal.initSettings
import inc.brody.words.ui.adapters.WordsAdapter
import inc.brody.words.ui.base.ScopedFragment
import inc.brody.words.ui.dialogs.AddWordDialog
import kotlinx.android.synthetic.main.fragment_words.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class WordsFragment : ScopedFragment(), KodeinAware {

    override val kodein: Kodein by closestKodein()

    private val mViewModelFactory : WordsViewModelFactory by instance()

    private lateinit var mViewModel: WordsViewModel

    private lateinit var mAdapter: WordsAdapter

    private lateinit var mDialog: AddWordDialog

    private lateinit var mObserver: Observer<List<WordEntry>>

    private var firestoreWords: LiveData<List<WordEntry>>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_words, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(this,mViewModelFactory)
            .get(WordsViewModel::class.java)

        mDialog = AddWordDialog(this.activity!!)
        mAdapter = WordsAdapter()

        mDialog.setTransferListener {
            bindButton(it.getValue("word"),it.getValue("desc"))
        }

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Words"

        mAdapter.setOnWordClickListener {
            bindDeleting(it)
        }

        bindUI()

        initRemoveWordsObserver()

        button_add_word.setOnClickListener { mDialog.show() }
    }

    private fun bindUI() = launch {
        val currentWords = mViewModel.words.await()

        if(mViewModel.prefsProvider.isSyncFirstLoad &&
                mViewModel.prefsProvider.isFullSyncNeeded){
            firestoreWords = mViewModel.firestoreWords.await()

            firestoreWords!!.observe(this@WordsFragment,mObserver)
        }

        recycler_view.layoutManager = LinearLayoutManager(this@WordsFragment.activity!!.applicationContext)
        recycler_view.adapter = mAdapter

        currentWords.observe(this@WordsFragment, Observer { words ->
            if(words == null) return@Observer

            handleList(words)
        })
    }

    private fun bindButton(word: String,desc: String) {
        if (word.isNotEmpty() && desc.isNotEmpty())
            mViewModel.addWord(WordEntry(word, desc, uid = mViewModel.prefsProvider.currentSyncUid))
    }


    private fun bindDeleting(word: WordEntry) = launch(Dispatchers.IO) {
        if (word.word.isNotEmpty())
            mViewModel.deleteWord(word)
    }

    private fun initRemoveWordsObserver(){
        mObserver = Observer { word->
            if(word == null) return@Observer

            Log.w(GoogleUtil.TAG,word.size.toString())

            addListOfWords(word)
            mViewModel.prefsProvider.isSyncFirstLoad = false
            firestoreWords?.removeObserver(mObserver)
        }
    }

    private fun addListOfWords(words: List<WordEntry>) = launch {
        mViewModel.addListOfWords(words)
    }

    private fun handleList(words: List<WordEntry>?) = launch {
        val currentList = mViewModel.commitSynchronizationAsync(words).await()

        mAdapter.setWords(currentList?.filter { it.isAlive })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_settings->initSettings()
        }
        return true
    }

}
