package de.schuettslaar.sensoration.presentation.views.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.schuettslaar.sensoration.presentation.views.devices.client.discovering.ClientDeviceRoute
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.MainDeviceRoute
import de.schuettslaar.sensoration.presentation.views.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
object HomeOverviewRoute

fun NavGraphBuilder.addHomeNavGraph(navController: NavHostController) {
    composable<HomeOverviewRoute> {
        HomeView(
            onAdvertising = {
                navController.navigate(MainDeviceRoute)
            },
            onDiscovering = {
                navController.navigate(ClientDeviceRoute)
            },
            onSettings = {
                navController.navigate(SettingsRoute)
            },
        )

    }
}