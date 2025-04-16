package de.schuettslaar.sensoration.presentation.views.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object HomeOverviewRoute

fun NavGraphBuilder.addHomeNavGraph(navController: NavHostController) {
    composable<HomeOverviewRoute> {
        HomeView(onBack = { navController.popBackStack() })
    }
}