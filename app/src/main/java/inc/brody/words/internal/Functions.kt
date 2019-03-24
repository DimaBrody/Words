package inc.brody.words.internal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import inc.brody.words.R

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

fun View.setFadeInAnimation(){
    visibility = View.GONE
    alpha = 0f
    animate()
        .setDuration(150)
        .setListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                visibility = View.VISIBLE
                super.onAnimationEnd(animation)
            }
        })
        .setStartDelay(100)
        .alpha(1f)
}


fun View.setFadeOutAnimation(){
    visibility = View.VISIBLE
    alpha = 1f
    animate()
        .setDuration(150)
        .setListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                visibility = View.GONE
                super.onAnimationEnd(animation)
            }
        })
        .alpha(0f)
}

fun setFadeInAnimationForViews(vararg views: View)
        = views.forEach { it.setFadeInAnimation() }

fun setFadeOutAnimationForViews(vararg views: View)
        = views.forEach { it.setFadeOutAnimation() }

