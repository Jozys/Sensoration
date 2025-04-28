package de.schuettslaar.sensoration.presentation.views.advertisment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.presentation.core.StatusInformation
import de.schuettslaar.sensoration.presentation.views.connected.ConnectedList
import de.schuettslaar.sensoration.presentation.views.home.HomeAppBar
import de.schuettslaar.sensoration.utils.getStringResourceByName

@Composable
fun Advertisement(onBack: () -> Unit) {

    var viewModel = viewModel<AdvertisementViewModel>()

    Scaffold(
        topBar = {
            HomeAppBar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusInformation(
                statusText = getStringResourceByName(
                    context = LocalContext.current,
                    resName = viewModel.status.name
                ).uppercase(),
            )
            ConnectedList(connectedDevices = viewModel.connectedDevices, onConfirmRemove = {
                viewModel.disconnect(it)
            })

            Button(onClick = {
                viewModel.stop()
                onBack()
            }) {
                Text(stringResource(R.string.stop_advertising))
            }
        }
    }
}