package de.schuettslaar.sensoration.presentation.views.advertisment

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object AdvertisementRoute

fun NavGraphBuilder.addAdvertisementNavGraph(navController: NavHostController) {
    composable<AdvertisementRoute> {
        Advertisement(onBack = { navController.popBackStack() })
    }
}