package de.schuettslaar.sensoration.presentation.views.settings

import androidx.lifecycle.ViewModel
import de.schuettslaar.sensoration.application.data.datastore.DataStoreServiceProvider
import de.schuettslaar.sensoration.presentation.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

class SettingsViewModel : ViewModel() {
    private val dataStoreService = DataStoreServiceProvider.getInstance()

    val theme: Flow<ThemeMode> = dataStoreService.theme

    val dataStore = dataStoreService.getDataStore()
    val themeKey = dataStoreService.themeKey
}