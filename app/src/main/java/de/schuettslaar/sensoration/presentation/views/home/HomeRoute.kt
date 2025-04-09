package de.schuettslaar.sensoration.views.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState

@Serializable
object HomeOverviewRoute

fun NavGraphBuilder.addHomeNavGraph(navController: NavHostController) {
    composable<HomeOverviewRoute> {
        HomeView(onBack = { navController.popBackStack() })
    }
}