package inc.brody.words.data.providers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import inc.brody.words.data.data.Constants
import inc.brody.words.internal.isOnline

class PreferenceProvider(ctx: Context) {
    private val appContext = ctx.applicationContext

    private val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(appContext)

    var isSyncTurned: Boolean
        get() = preferences.getBoolean(Constants.SYNC, false)
        set(value) = editPreference(Constants.SYNC, value)

    var isSyncNeeded: Boolean
        get() = preferences.getBoolean(Constants.SYNC_NEEDED, false)
        set(value) = editPreference(Constants.SYNC_NEEDED, value)


    var isSyncFirstLoad: Boolean
        get() = preferences.getBoolean(Constants.SYNC_FIRST, false)
        set(value) = editPreference(Constants.SYNC_FIRST, value)

    var currentSyncUid: String?
        get() = preferences.getString(Constants.SYNC_UID, null)
        set(value) = editPreference(Constants.SYNC_UID, value, 1)

    val isFullSyncNeeded: Boolean
        get() = isSyncTurned && isOnline(appContext) && isSyncNeeded

    val isParticularSyncNeeded: Boolean
        get() = isSyncTurned && isOnline(appContext)


    fun setSyncConditions(condition: ()->Unit){
        isSyncNeeded = true
        condition()
    }

    private fun editPreference(name: String, value: Any?, type: Int = 0) {
        val prefsEditor = preferences.edit()
        when (type) {
            0 -> prefsEditor.putBoolean(name, value as Boolean).apply()
            1 -> prefsEditor.putString(name, value as String?).apply()
        }
    }
}