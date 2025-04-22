package de.schuettslaar.sensoration.presentation.views.advertisment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.presentation.views.connected.ConnectedList
import de.schuettslaar.sensoration.presentation.views.home.HomeAppBar

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
            Text(
                text = stringResource(R.string.current_status, viewModel.status),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            Text(stringResource(R.string.advertising_title))
            ConnectedList(connectedDevice = viewModel.connectedDevices, onConfirmRemove = {
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