package de.schuettslaar.sensoration.presentation.views.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.schuettslaar.sensoration.presentation.views.advertisment.AdvertisementRoute
import de.schuettslaar.sensoration.presentation.views.discovering.DiscoveryRoute
import kotlinx.serialization.Serializable

@Serializable
object HomeOverviewRoute

fun NavGraphBuilder.addHomeNavGraph(navController: NavHostController) {
    composable<HomeOverviewRoute> {
        HomeView(
            onAdvertising = {
                navController.navigate(AdvertisementRoute)
            },
            onDiscovering = {
                navController.navigate(DiscoveryRoute)
            },
        )

    }
}