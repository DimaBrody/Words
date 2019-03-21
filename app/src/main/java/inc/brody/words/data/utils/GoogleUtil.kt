package inc.brody.words.data.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.IdToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import inc.brody.words.R
import inc.brody.words.data.db.entity.GoogleUser
import inc.brody.words.internal.GoogleUserException
import inc.brody.words.internal.toast
import java.lang.Exception

object GoogleUtil {

    const val TAG = "GoogleUtil"
    const val RC_GOOGLE_SIGN_IN = 9000

    fun signInGoogle(
        ctx: Context,
        idToken: String? = ctx.getString(R.string.token_id),
        options: GoogleSignInOptions = GoogleSignInOptions.DEFAULT_SIGN_IN,
        onComplete: (Intent) -> Unit
    ) {
        val signInActivity = GoogleSignIn.getClient(
            ctx, GoogleSignInOptions.Builder(options).requestIdToken(idToken)
                .requestEmail().build()
        ).signInIntent
        onComplete(signInActivity)
    }

    fun handleActivityResult(
        task: Task<GoogleSignInAccount>,
        context: Context,
        onComplete: () -> Unit = {}
    ) {
        val account = task.getResult(ApiException::class.java)
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        FirebaseUtil.auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseUtil.currentUserDocRef.get().addOnSuccessListener { cur ->
                    if (!cur.exists()) {
                        val newUser = GoogleUtil.getGoogleUser(context)
                        FirebaseUtil.currentUserDocRef.set(newUser ?: throw GoogleUserException())
                            .addOnSuccessListener {
                                onComplete()
                            }
                    } else {
                        onComplete()
                    }
                }
            }
        }

    }

    fun signOutGoogle(googleApiClient: GoogleApiClient?,onComplete: () -> Unit = {}) {
        try {
            Auth.GoogleSignInApi.signOut(googleApiClient)
        } catch (e: Exception) {
            Log.w(TAG, "Sign Out", e)
        }
        FirebaseUtil.auth.signOut()
        onComplete()
    }

    fun getGoogleUser(ctx: Context): GoogleUser? {
        var user: GoogleUser? = null
        googleUserBuff(ctx) {
            user = it
        }
        return user
    }

    fun getGoogleApiClient(activity: FragmentActivity) : GoogleApiClient? =
        GoogleApiClient.Builder(activity).enableAutoManage(activity) {
            Log.d("TAG", "Connection failed:$it")
        }.addApi(Auth.GOOGLE_SIGN_IN_API).build()



    private fun googleUserBuff(ctx: Context, onComplete: (GoogleUser) -> Unit) {
        GoogleSignIn.getLastSignedInAccount(ctx)?.let {
            onComplete(
                GoogleUser(
                    it.displayName,
                    it.givenName,
                    it.familyName,
                    it.email,
                    it.id,
                    it.photoUrl?.toString(),
                    FirebaseUtil.uid
                )
            )
        }
    }


}