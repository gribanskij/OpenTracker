package com.gribansky.opentracker.core

import android.content.SharedPreferences
import com.gribansky.opentracker.ui.TrackerState
import kotlinx.serialization.json.Json


private const val TRACKER_STATE_KEY = "tracker_state"

class SharePrefManager (private val pref: SharedPreferences):IPrefManager {
    override var state: TrackerState
        get() {
            val str = pref.getString(TRACKER_STATE_KEY,"")
            return if (str!!.isNotEmpty()) Json.decodeFromString<TrackerState>(str)
            else TrackerState()
        }
        set(value) {
            val str = Json.encodeToString(value)
            pref.edit().run {
                putString(TRACKER_STATE_KEY,str)
                apply()
            }
        }
}