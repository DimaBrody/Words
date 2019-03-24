package inc.brody.words.ui.words


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager

import inc.brody.words.R
import inc.brody.words.data.db.entity.WordEntry
import inc.brody.words.data.helpers.ActionModeCallback
import inc.brody.words.data.helpers.SwipeItemTouchHelper
import inc.brody.words.data.utils.GoogleUtil
import inc.brody.words.ui.adapters.WordsAdapter
import inc.brody.words.ui.base.ScopedFragment
import inc.brody.words.ui.dialogs.AddWordDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_words.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class WordsFragment : ScopedFragment(), KodeinAware {

    override val kodein: Kodein by closestKodein()

    private val mViewModelFactory: WordsViewModelFactory by instance()

    private lateinit var mViewModel: WordsViewModel

    private lateinit var mAdapter: WordsAdapter

    private lateinit var mDialog: AddWordDialog

    private lateinit var mItemTouchHelper: ItemTouchHelper

    private lateinit var actionModeCallback: ActionModeCallback

    private lateinit var mObserver: Observer<List<WordEntry>>

    private var firestoreWords: LiveData<List<WordEntry>>? = null

    private var actionMode: ActionMode? = null

    var deleteListener: OnCallbackDestroy? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_words, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(this, mViewModelFactory)
            .get(WordsViewModel::class.java)

        mDialog = AddWordDialog(this.activity!!)
        mAdapter = WordsAdapter(context!!.applicationContext)

        mDialog.setTransferListener(
            wordCallback = {
                bindButton(it.getValue("word"), it.getValue("desc"))
            },
            listCallback = {
                addListOfWords(it,true)
            })

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Words"

        initCallback()

        bindUI()

        initAdapterListeners()

        initRemoveWordsObserver()

        initActionModeCallback()

        initCallbackDestroy()

        button_add_word.setOnClickListener { mDialog.show() }
    }

    private fun bindUI() = launch {
        Runnable { progressBar.visibility = View.VISIBLE }.run()
        val currentWords = mViewModel.words.await()

        if (mViewModel.prefsProvider.isSyncFirstLoad &&
            mViewModel.prefsProvider.isFullSyncNeeded
        ) {
            firestoreWords = mViewModel.firestoreWords.await()

            firestoreWords!!.observe(this@WordsFragment, mObserver)
        }

        recycler_view.layoutManager = LinearLayoutManager(this@WordsFragment.activity!!.applicationContext)
        recycler_view.adapter = mAdapter

        currentWords.observe(this@WordsFragment, Observer { words ->
            progressBar.visibility = View.GONE
            if (words == null) return@Observer

            handleList(words)
        })
    }

    private fun initAdapterListeners() {
        mAdapter.setOnWordClickListener(
            deleteCallback = {
                bindDeleting(it)
                deleteListener?.destroyCallback()
            },
            longCallback = {
                enableActionMode(it)
            },
            clickCallback = {
                if (mAdapter.getSelectedItemCount() > 0)
                    enableActionMode(it)
                else {
                    //TODO
                }
            })
    }

    private fun bindButton(word: String, desc: String) {
        if (word.isNotEmpty() && desc.isNotEmpty()) {
            mViewModel.addWord(WordEntry(word, desc, uid = mViewModel.prefsProvider.currentSyncUid))
            progressBar.visibility = View.VISIBLE
        }

    }


    private fun bindDeleting(
        word: WordEntry? = null,
        words: List<WordEntry>? = null
    ) = launch {
        mViewModel.deleteWords(word = word, words = words)
        progressBar.visibility = View.VISIBLE
    }

    private fun initRemoveWordsObserver() {
        mObserver = Observer { word ->
            if (word == null) return@Observer

            Log.w(GoogleUtil.TAG, word.size.toString())

            addListOfWords(word)
            mViewModel.prefsProvider.isSyncFirstLoad = false
            progressBar.visibility = View.GONE
            firestoreWords?.removeObserver(mObserver)
        }
    }

    private fun initCallbackDestroy() {
        deleteListener = object : OnCallbackDestroy {
            override fun destroyCallback() {
                actionModeCallback.destroyCallback()
            }
        }
    }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(actionModeCallback)
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        mAdapter.toggleSelection(position)
        val count = mAdapter.getSelectedItemCount()

        if (count == 0)
            actionMode?.finish()
        else {
            actionMode?.title = count.toString()
            actionMode?.invalidate()
        }
    }

    private fun deleteAllWords() {
        val selectedItemPositions = mAdapter.getSelectedItems()
        val list = mutableListOf<WordEntry>()

        for (i in selectedItemPositions.size - 1 downTo 0) {
            list.add(mAdapter.getItem(selectedItemPositions[i]))
            mAdapter.removeData(selectedItemPositions[i])
        }
        bindDeleting(words = list)
        mAdapter.notifyDataSetChanged()
    }

    private fun initActionModeCallback() {
        actionModeCallback = ActionModeCallback(activity!!)
        actionModeCallback.setOnDestroyListener(
            deleteCallback = {
                deleteAllWords()
            },
            destroyCallback = {
                mAdapter.clearSelections()
                actionMode = null
            }
        )
    }

    private fun addListOfWords(words: List<WordEntry>,isFirestoreNeeded: Boolean = false) = launch {
        mViewModel.addListOfWords(words,isFirestoreNeeded)
    }

    private fun handleList(words: List<WordEntry>?) = launch {
        val currentList = mViewModel.commitSynchronizationAsync(words).await()

        mAdapter.setWords(currentList?.filter { it.isAlive } as ArrayList<WordEntry>?)
    }

    private fun initCallback() {
        val callback = SwipeItemTouchHelper(mAdapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(recycler_view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val options = navOptions {
            anim {
                enter = R.anim.fade_in
                exit = R.anim.fade_out
                popEnter = R.anim.fade_in
                popExit = R.anim.fade_out
            }
        }
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(R.id.settingsFragment, null, options)
        }
        return true
    }

    interface OnCallbackDestroy {
        fun destroyCallback()
    }

}
