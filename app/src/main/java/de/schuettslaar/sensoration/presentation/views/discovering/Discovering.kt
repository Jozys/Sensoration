package de.schuettslaar.sensoration.presentation.views.discovering

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.presentation.core.StatusInformation
import de.schuettslaar.sensoration.presentation.views.home.HomeAppBar

@Composable
fun Discovering(onBack: () -> Unit) {

    var viewModel = viewModel<DiscoveringViewModel>()

    Scaffold(
        topBar = {
            HomeAppBar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (viewModel.isLoading) {
                StatusInformation(
                    statusText = viewModel.status.name,
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            } else {
                if (viewModel.connectedDevices.isEmpty()) {
                    DiscoveringState(
                        connect = {
                            viewModel.connect(it)
                        },
                        stop = {
                            viewModel.stop()
                            onBack()
                        },
                        possibleConnections = viewModel.possibleConnections,
                        status = viewModel.status
                    )
                }

                if (viewModel.connectedDevices.isNotEmpty()) {
                    ConnectedState(
                        viewModel.connectedDevices.entries.first(),
                        sendMessage = {
                            viewModel.sendMessage()
                        },
                        disconnect = {
                            viewModel.disconnect()
                        },
                        deviceStatus = viewModel.device?.applicationStatus
                            ?: ApplicationStatus.ERROR
                    )
                }
            }
        }
    }
}

@Composable
fun BuildDeviceEntry(
    id: String,
    info: DiscoveredEndpointInfo,
    onClick: (id: String) -> Unit,
) {
    ListItem(modifier = Modifier.clickable {
        onClick(id)
    }, trailingContent = {
        Icon(Icons.Default.Info, "Not Connected")
    }, headlineContent = {
        Text("Device: ${info.endpointName} (ID: $id)")
    })

}

@Composable
fun DiscoveringState(
    connect: (String) -> Unit,
    stop: () -> Unit,
    possibleConnections: Map<String, DiscoveredEndpointInfo>,
    status: NearbyStatus
) {
    StatusInformation(
        statusText = status.name,
    )
    Text(
        modifier = Modifier.padding(8.dp),
        text = stringResource(R.string.discovering_title)
    )

    LazyColumn {
        items(possibleConnections.entries.toList()) { (id, info) ->
            BuildDeviceEntry(id, info, {
                connect(id)
            })
        }
    }

    Button(onClick = {
        stop()
    }) {
        Text(
            text = stringResource(R.string.stop_discovering)
        )
    }

}

@Composable
fun ConnectedState(
    entry: Map.Entry<String, String>,
    sendMessage: () -> Unit,
    disconnect: () -> Unit,
    deviceStatus: ApplicationStatus
) {
    StatusInformation(
        statusText = deviceStatus.name,
    )

    ConnectedDevice(
        entry.value
    )

    Button(onClick = {
        sendMessage()
    }) {
        Text(
            text = stringResource(R.string.debug_send_message)
        )
    }
    Button(onClick = {
        disconnect()
    }) {
        Text(
            text = stringResource(R.string.disconnect_from_device)
        )
    }
}

@Composable
fun ConnectedDevice(name: String) {
    Column(modifier = Modifier.padding(8.dp)) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = "Connected",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )
    }
}