package inc.brody.words.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import inc.brody.words.R
import inc.brody.words.data.db.entity.WordEntry
import inc.brody.words.data.helpers.SwipeItemTouchHelper
import kotlinx.android.synthetic.main.item_word_recycler.view.*
import kotlinx.android.synthetic.main.item_word_undo.view.*

@Suppress("TYPEALIAS_EXPANSION_DEPRECATION")
class WordsAdapter(private val ctx: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(), SwipeItemTouchHelper.SwipeHelperAdapter {
    private var words = ArrayList<WordEntry>()
    private val words_swiped = ArrayList<WordEntry>()

    private val selectedItems = SparseBooleanArray()
    private var current_selected_idx = -1

    var wordClickListener: OnWordClickListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentWord = words[position]
        with(holder.itemView) {
            wordMain.text = currentWord.word
            wordDesc.text = currentWord.desc
            btn_clap.setOnClickListener {
                // wordClickListener?.onWordDeleted(currentWord)
            }

            bt_undo.setOnClickListener {
                words[position].swiped = false
                words_swiped.remove(words[position])
                notifyItemChanged(position)
            }

            lyt_parent.setOnLongClickListener {
                wordClickListener?.onItemLongClick(position)
                return@setOnLongClickListener true
            }

            lyt_parent.setOnClickListener {
                wordClickListener?.onWordClicked(position)
            }

            if (currentWord.swiped)
                lyt_parent.visibility = View.GONE
            else
                lyt_parent.visibility = View.VISIBLE
        }
        toggleCheckedIcon(holder as ViewHolder,position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_word_recycler, parent, false))

    override fun getItemCount(): Int = words.size

    fun setWords(words: ArrayList<WordEntry>?) {
        words?.let {
            this.words = it
            notifyDataSetChanged()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                for (word in words_swiped) {
                    val index_removed = words.indexOf(word)
                    if (index_removed != -1) {
                        words.removeAt(index_removed)
                        notifyItemRemoved(index_removed)
                    }
                }
                words_swiped.clear()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onItemDismiss(position: Int) {
        if (words[position].swiped) {
            wordClickListener?.onWordDeleted(words[position])
            words_swiped.remove(words[position])
            words.removeAt(position)
            notifyItemRemoved(position)
            return
        }

        words[position].swiped = true
        words_swiped.add(words[position])
        notifyItemChanged(position)
    }

    private fun toggleCheckedIcon(holder: ViewHolder, position: Int) {
        Log.w("HelloWorld",selectedItems[position].toString())
        with(holder.itemView){
            if (selectedItems[position, false]) {
                lyt_parent.background = ctx.resources.getDrawable(R.color.grey_10)
                btn_check.visibility = View.VISIBLE
                btn_clap.visibility = View.GONE
                if(current_selected_idx == position) resetCurrentIndex()
            } else {
                lyt_parent.background = ctx.resources.getDrawable(android.R.color.white)
                btn_clap.visibility = View.VISIBLE
                btn_check.visibility = View.GONE
                if(current_selected_idx == position) resetCurrentIndex()
            }
        }
    }

    fun getItem(position: Int) = words[position]

    fun toggleSelection(position: Int) {
        current_selected_idx = position
        if (selectedItems[position, false])
            selectedItems.delete(position)
        else
            selectedItems.put(position, true)
        notifyItemChanged(position)
    }

    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItemCount() = selectedItems.size()

    fun getSelectedItems(): List<Int> {
        val items = ArrayList<Int>(selectedItems.size())
        for (i in 0 until selectedItems.size())
            items.add(selectedItems.keyAt(i))

        return items
    }

    fun removeData(position: Int) {
        words.removeAt(position)
        resetCurrentIndex()
    }

    private fun resetCurrentIndex() {
        current_selected_idx = -1
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), SwipeItemTouchHelper.TouchViewHolder {

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.GRAY)
        }

    }

    inline fun setOnWordClickListener(
        crossinline deleteCallback: (WordEntry) -> Unit = {},
        crossinline longCallback: (Int) -> Unit = {},
        crossinline clickCallback: (Int)-> Unit = {}
    ) {
        wordClickListener = object : OnWordClickListener {
            override fun onWordDeleted(word: WordEntry) {
                deleteCallback(word)
            }

            override fun onItemLongClick(position: Int) {
                longCallback(position)
            }

            override fun onWordClicked(position: Int) {
                clickCallback(position)
            }
        }
    }

    interface OnWordClickListener {
        fun onWordDeleted(word: WordEntry)

        fun onWordClicked(position: Int)

        fun onItemLongClick(position: Int)
    }
}