package de.schuettslaar.sensoration.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import de.schuettslaar.sensoration.presentation.views.devices.client.discovering.addDiscoveryNavGraph
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.addAdvertisementNavGraph
import de.schuettslaar.sensoration.presentation.views.home.HomeOverviewRoute
import de.schuettslaar.sensoration.presentation.views.home.addHomeNavGraph
import de.schuettslaar.sensoration.presentation.views.settings.addSettingsNavGraph

@Composable()
fun SensorationNavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = HomeOverviewRoute,
        modifier = modifier
    ) {
        addHomeNavGraph(navController)
        addDiscoveryNavGraph(navController)
        addAdvertisementNavGraph(navController)
        addSettingsNavGraph(navController)
    }
}