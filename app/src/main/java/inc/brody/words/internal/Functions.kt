package inc.brody.words.internal

import android.content.Context
import android.net.ConnectivityManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText

fun coordinateButtonWithInputs(btn: Button, vararg edits: EditText) {
    val watcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable?) {
            btn.isEnabled = edits.all { it.text.isNotEmpty() }
        }
    }
    edits.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = edits.all { it.text.isNotEmpty() }
}

fun isOnline(ctx: Context): Boolean {
    val connectivityManager = ctx.applicationContext
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

