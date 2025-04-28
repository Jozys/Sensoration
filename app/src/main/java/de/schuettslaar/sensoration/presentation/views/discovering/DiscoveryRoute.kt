package de.schuettslaar.sensoration.presentation.views.discovering

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object DiscoveryRoute

fun NavGraphBuilder.addDiscoveryNavGraph(navController: NavHostController) {
    composable<DiscoveryRoute> {
        Discovering(onBack = { navController.popBackStack() })
    }
}