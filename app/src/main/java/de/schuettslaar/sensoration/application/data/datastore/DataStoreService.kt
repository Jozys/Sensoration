package de.schuettslaar.sensoration.application.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import de.schuettslaar.sensoration.dataStore
import de.schuettslaar.sensoration.presentation.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreService(context: Context) {

    private val dataStore = context.dataStore;

    val themeKey = stringPreferencesKey("theme")

    val theme: Flow<ThemeMode> = dataStore.data.map { preferences ->
        ThemeMode.valueOf(preferences[themeKey] ?: ThemeMode.SYSTEM.toString())
    }

    suspend fun setTheme(theme: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeKey] = theme.toString()
        }
    }

    fun getDataStore() = dataStore
}