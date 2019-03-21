package inc.brody.words.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import inc.brody.words.R
import inc.brody.words.data.db.entity.WordEntry
import kotlinx.android.synthetic.main.item_word_recycler.view.*

class WordsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var words = emptyList<WordEntry>()

    var wordClickListener : OnWordClickListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentWord = words[position]
        with(holder.itemView){
            wordMain.text = currentWord.word
            wordDesc.text = currentWord.desc
            btn_clap.setOnClickListener {
                wordClickListener?.wordClicked(currentWord)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_word_recycler,parent,false))

    override fun getItemCount(): Int = words.size

    inline fun setOnWordClickListener(crossinline callback: (WordEntry)->Unit){
        wordClickListener = object : OnWordClickListener {
            override fun wordClicked(word: WordEntry) {
                callback(word)
            }
        }
    }

    fun setWords(words: List<WordEntry>?){
        words?.let {
            this.words = it
            notifyDataSetChanged()
        }
    }


    interface OnWordClickListener {
        fun wordClicked(word: WordEntry)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}