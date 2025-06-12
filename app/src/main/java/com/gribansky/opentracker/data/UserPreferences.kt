package com.gribansky.opentracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(
    val username: String = "",
    val password: String = "",
    val serverAddress: String = "",
    val useWorkTime: Boolean = true
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PreferencesKeys {
    val USERNAME = stringPreferencesKey("username")
    val PASSWORD = stringPreferencesKey("password")
    val SERVER_ADDRESS = stringPreferencesKey("server_address")
    val USE_WORK_TIME = booleanPreferencesKey("use_work_time")
}

fun DataStore<Preferences>.getUserPreferencesFlow(): Flow<UserPreferences> {
    return data.map { preferences ->
        UserPreferences(
            username = preferences[PreferencesKeys.USERNAME] ?: "",
            password = preferences[PreferencesKeys.PASSWORD] ?: "",
            serverAddress = preferences[PreferencesKeys.SERVER_ADDRESS] ?: "",
            useWorkTime = preferences[PreferencesKeys.USE_WORK_TIME] ?: true
        )
    }
}

suspend fun DataStore<Preferences>.updateUserPreferences(
    username: String? = null,
    password: String? = null,
    serverAddress: String? = null,
    useWorkTime: Boolean? = null
) {
    edit { preferences ->
        username?.let { preferences[PreferencesKeys.USERNAME] = it }
        password?.let { preferences[PreferencesKeys.PASSWORD] = it }
        serverAddress?.let { preferences[PreferencesKeys.SERVER_ADDRESS] = it }
        useWorkTime?.let { preferences[PreferencesKeys.USE_WORK_TIME] = it }
    }
} 