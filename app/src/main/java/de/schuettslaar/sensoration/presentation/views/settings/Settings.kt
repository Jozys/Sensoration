package de.schuettslaar.sensoration.presentation.views.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamal.composeprefs3.ui.GroupHeader
import com.jamal.composeprefs3.ui.PrefsScreen
import com.jamal.composeprefs3.ui.prefs.DropDownPref
import com.jamal.composeprefs3.ui.prefs.TextPref
import de.schuettslaar.sensoration.BuildConfig
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.presentation.core.textDialog.TextDialog
import de.schuettslaar.sensoration.presentation.core.textDialog.TextDialogInfo
import de.schuettslaar.sensoration.presentation.ui.theme.ThemeMode

@Composable
fun Settings(
    onBack: () -> Unit,
) {
    var viewModel: SettingsViewModel = viewModel<SettingsViewModel>()

    Scaffold(topBar = { SettingsAppBar(onBack = onBack) }) { innerPadding ->
        SettingsContent(
            modifier = Modifier.padding(innerPadding),
            dataStore = viewModel.dataStore,
            themeKey = viewModel.themeKey,
            theme = viewModel.theme.toString()
        )
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    dataStore: DataStore<Preferences>,
    themeKey: Preferences.Key<String>,
    theme: String = ThemeMode.SYSTEM.toString(),
) {
    var openDialog by remember { mutableStateOf<TextDialogInfo?>(null) }

    AnimatedVisibility(openDialog != null) {
        openDialog?.let { dialogInfo ->
            TextDialog(
                dialogInfo = dialogInfo,
                onDismissRequest = { openDialog = null },
                modifier = Modifier
            )
        }
    }


    PrefsScreen(
        dataStore = dataStore,
        modifier = modifier,
    ) {
        prefsGroup({
            GroupHeader(
                title = "App Settings"
            )
        }) {
            prefsItem {
                DropDownPref(
                    title = "Theme",
                    useSelectedAsSummary = true,
                    enabled = true,
                    key = themeKey.toString(),
                    defaultValue = theme.toString(),
                    entries = mapOf(
                        ThemeMode.SYSTEM.toString() to stringResource(R.string.settings_dark_theme_system),
                        ThemeMode.LIGHT.toString() to stringResource(R.string.settings_dark_theme_light),
                        ThemeMode.DARK.toString() to stringResource(R.string.settings_dark_theme_dark)
                    ),
                    dropdownBackgroundColor = MaterialTheme.colorScheme.background,
                )
            }
        }

        prefsGroup({
            GroupHeader(
                title = stringResource(R.string.settings_about)
            )
        }) {
            prefsItem {
                TextPref(
                    title = stringResource(R.string.settings_about_app),
                    summary = stringResource(R.string.settings_about_app_desc),
                    enabled = true,
                    onClick = {
                        openDialog = TextDialogInfo(
                            titleResId = R.string.settings_about_app_dialog_header,
                            textResIds = listOf(
                                R.string.settings_about_app_dialog_text,
                            )
                        )
                    },
                )
            }
            prefsItem {
                TextPref(
                    title = stringResource(R.string.settings_about_devs),
                    summary = stringResource(R.string.settings_about_dev_desc),
                    enabled = true,
                    onClick = {
                        openDialog = TextDialogInfo(
                            titleResId = R.string.settings_about_devs_dialog_header,
                            textResIds = listOf(
                                R.string.settings_about_devs_dialog_text,
                            )
                        )
                    },
                )
            }
        }

        prefsItem {
            TextPref(
                title = stringResource(R.string.settings_app_version),
                summary = BuildConfig.VERSION_NAME
            )
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings_desc),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp) // Add padding to the title
            )
        },
        navigationIcon =
            @Composable {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(8.dp) // Add padding to the icon
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
        actions = { /* TODO: Add actions */ },
        modifier = Modifier.padding(8.dp) // Add padding to the top app bar
    )
}