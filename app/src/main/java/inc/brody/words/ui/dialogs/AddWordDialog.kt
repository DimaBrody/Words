package inc.brody.words.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import inc.brody.words.R
import inc.brody.words.internal.coordinateButtonWithInputs
import kotlinx.android.synthetic.main.add_word_dialog.*

class AddWordDialog(context: Context) : Dialog(context) {

    override fun show() {
        super.show()
        et_desc.setText("")
        et_word.setText("")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.add_word_dialog)
        setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT

        coordinateButtonWithInputs(bt_submit,et_desc,et_word)

        bt_submit.setOnClickListener {
            dismiss()
            transferListener?.transferFields(et_word.text.toString(),et_desc.text.toString())
        }

        window!!.attributes = lp
    }

    interface OnTransferInfoListener {
        fun transferFields(wordString: String,descString: String)
    }

    var transferListener: OnTransferInfoListener? = null

    inline fun setTransferListener(crossinline callback: (Map<String,String>)->Unit){
        transferListener = object : OnTransferInfoListener {
            override fun transferFields(wordString: String, descString: String) {
                callback(mapOf("word" to wordString,"desc" to descString))
            }
        }
    }

}