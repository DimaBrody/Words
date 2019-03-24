package inc.brody.words.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import inc.brody.words.R
import inc.brody.words.data.db.entity.WordEntry
import inc.brody.words.internal.*
import kotlinx.android.synthetic.main.add_word_dialog.*

class AddWordDialog(context: Context) : Dialog(context) {

    private lateinit var imm : InputMethodManager

    private var isEditEnabled = false

    private var currentNumber = 0

    private val wordsList = mutableListOf(
        WordEntry("Один", "One"),
        WordEntry("Два", "Two"),
        WordEntry("Три", "Three"),
        WordEntry("Четыре", "Four"),
        WordEntry("Пять", "Five"),
        WordEntry("Шесть", "Six"),
        WordEntry("Семь", "Seven"),
        WordEntry("Восемь", "Eight"),
        WordEntry("Девять", "Nine"),
        WordEntry("Десять", "Ten")
    )

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

        coordinateButtonWithInputs(bt_submit, et_desc, et_word)

        imm = (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)

        bt_submit.setOnClickListener {
            dismiss()
            if (!isEditEnabled)
                transferListener?.transferFields(et_word.text.toString(), et_desc.text.toString())
            else {
                transferListener?.transferList(wordsList.take(currentNumber))
                isEditEnabled = false
            }
        }

        initNumberPicker()

        bt_create.setOnClickListener {
            isEditEnabled = !isEditEnabled
            imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).rootView.windowToken,0)
            changeVisibility(isEditEnabled)
        }

        bt_cancel.setOnClickListener {
            isEditEnabled = !isEditEnabled
            changeVisibility(isEditEnabled)
        }

        window!!.attributes = lp
    }

    private fun changeVisibility(isEdit: Boolean) {
        if (isEdit) {
            setFadeInAnimationForViews(bt_cancel, relative_picker)
            setFadeOutAnimationForViews(bt_create, relative_edit,text_random)
            if (currentNumber != 0) bt_submit.isEnabled = true
        } else {
            setFadeInAnimationForViews(bt_create, relative_edit,text_random)
            setFadeOutAnimationForViews(bt_cancel, relative_picker)
            if (et_desc.text.isEmpty() || et_word.text.isEmpty())
                bt_submit.isEnabled = false
        }
    }

    private fun setDefaultVisibility(){
        bt_cancel.visibility = View.GONE
        bt_create.visibility = View.VISIBLE
        relative_picker.visibility = View.GONE
        relative_edit.visibility = View.VISIBLE
        text_random.visibility = View.VISIBLE
    }

    interface OnTransferInfoListener {
        fun transferFields(wordString: String, descString: String)
        fun transferList(words: List<WordEntry>)
    }

    private fun initNumberPicker() {
        with(number_picker) {
            maxValue = 10
            minValue = 0
            value = 1
            isFadingEdgeEnabled = true
            setOnValueChangedListener { picker, oldVal, newVal ->
                if (newVal != 0) {
                    currentNumber = newVal
                    bt_submit.isEnabled = true
                } else
                    bt_submit.isEnabled = false
            }
        }
    }

    var transferListener: OnTransferInfoListener? = null

    inline fun setTransferListener(
        crossinline wordCallback: (Map<String, String>) -> Unit = {},
        crossinline listCallback: (List<WordEntry>) -> Unit = {}
    ) {
        transferListener = object : OnTransferInfoListener {
            override fun transferFields(wordString: String, descString: String) {
                wordCallback(mapOf("word" to wordString, "desc" to descString))
            }

            override fun transferList(words: List<WordEntry>) {
                listCallback(words)
            }
        }
    }

}