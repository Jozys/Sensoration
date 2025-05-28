package de.schuettslaar.sensoration.presentation.views.devices.main.advertisment

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object MainDeviceRoute

fun NavGraphBuilder.addAdvertisementNavGraph(navController: NavHostController) {
    composable<MainDeviceRoute> {
        val viewModel: MainDeviceViewModel = viewModel()
        MainDeviceScreen(onBack = {
            viewModel.cleanUp()
            navController.popBackStack()
        })
    }
}