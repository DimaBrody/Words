package inc.brody.words.data.helpers

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode

import inc.brody.words.R
import inc.brody.words.ui.words.WordsFragment

class ActionModeCallback(private val act: Activity) : ActionMode.Callback, WordsFragment.OnCallbackDestroy {

    var destroyListener : OnDestroyListener? = null

    private var actionMode: ActionMode? = null

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        changeSystemBarColor(android.R.color.black)
        mode?.menuInflater?.inflate(R.menu.menu_delete,menu)

        actionMode = mode

        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        //(act as AppCompatActivity).supportActionBar?.hide()
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_delete->{
                destroyListener?.deleteWords()
                mode?.finish()
                return true
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        /*adapter.clearSelections()
        actionMode = null*/
        //(act as AppCompatActivity).supportActionBar?.show()
        actionMode = null
        destroyListener?.destroy()
        changeSystemBarColor(android.R.color.black)
    }

    private fun changeSystemBarColor(@ColorRes color: Int){
        (act as? AppCompatActivity)?.supportActionBar
            ?.setBackgroundDrawable(ColorDrawable(act.resources.getColor(color)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = act.resources.getColor(color)
        }
    }

    inline fun setOnDestroyListener(
        crossinline destroyCallback: ()->Unit = {},
        crossinline deleteCallback: ()->Unit = {}
    ){
        destroyListener = object : OnDestroyListener {
            override fun destroy() {
                destroyCallback()
            }

            override fun deleteWords() {
                deleteCallback()
            }
        }
    }

    override fun destroyCallback() {
        actionMode?.finish()
        destroyListener?.destroy()
    }

    interface OnDestroyListener {
        fun destroy()
        fun deleteWords()
    }
}