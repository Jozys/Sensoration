package de.schuettslaar.sensoration.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import de.schuettslaar.sensoration.views.home.HomeOverviewRoute
import de.schuettslaar.sensoration.views.home.addHomeNavGraph

@Composable()
fun SensorationNavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    NavHost(
        navController = navController,
        startDestination = HomeOverviewRoute,
        modifier = modifier) {
        addHomeNavGraph(navController)
    }
}