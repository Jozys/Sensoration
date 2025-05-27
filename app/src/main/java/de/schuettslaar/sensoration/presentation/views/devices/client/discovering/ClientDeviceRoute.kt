package de.schuettslaar.sensoration.presentation.views.devices.client.discovering

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object ClientDeviceRoute

fun NavGraphBuilder.addDiscoveryNavGraph(navController: NavHostController) {
    composable<ClientDeviceRoute> {
        ClientDeviceScreen(onBack = { navController.popBackStack() })
    }
}