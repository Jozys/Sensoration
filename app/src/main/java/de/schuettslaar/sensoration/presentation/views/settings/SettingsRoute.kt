package de.schuettslaar.sensoration.presentation.views.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

fun NavGraphBuilder.addSettingsNavGraph(navController: NavHostController) {
    composable<SettingsRoute> {
        Settings(onBack = { navController.popBackStack() })
    }
}