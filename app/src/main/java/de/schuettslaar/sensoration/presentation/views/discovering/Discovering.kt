package de.schuettslaar.sensoration.presentation.views.discovering

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.core.StatusInformation
import de.schuettslaar.sensoration.presentation.core.sensor.CurrentSensor
import de.schuettslaar.sensoration.presentation.views.home.HomeAppBar
import de.schuettslaar.sensoration.utils.getStringResourceByName

@Composable
fun Discovering(onBack: () -> Unit) {

    var viewModel = viewModel<DiscoveringViewModel>()

    Scaffold(
        topBar = {
            HomeAppBar(onSettings = { /* Not required for this screen */ })
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
                        deviceStatus = viewModel.thisApplicationStatus,
                        sensorType = viewModel.currentSensorType
                    )
                }
            }
        }
    }
}

@Composable
fun BuildDeviceEntry(
    id: DeviceId,
    info: DiscoveredEndpointInfo,
    onClick: (id: DeviceId) -> Unit,
) {
    ListItem(modifier = Modifier.clickable {
        onClick(id)
    }, trailingContent = {
        Icon(Icons.Default.Info, stringResource(R.string.no_connected_devices))
    }, headlineContent = {
        Text("Device: ${info.endpointName} (ID: $id)")
    })

}

@Composable
fun DiscoveringState(
    connect: (DeviceId) -> Unit,
    stop: () -> Unit,
    possibleConnections: Map<DeviceId, DiscoveredEndpointInfo>,
    status: NearbyStatus
) {
    StatusInformation(
        statusText = getStringResourceByName(
            context = LocalContext.current,
            resName = status.name
        ).uppercase(),
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
    entry: Map.Entry<DeviceId, String>,
    sendMessage: () -> Unit,
    disconnect: () -> Unit,
    sensorType: SensorType? = null,
    deviceStatus: ApplicationStatus
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.outlineVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
    ) {
        StatusInformation(
            statusText = deviceStatus.name,
            modifier = Modifier
                .padding(top = 8.dp)
        )

        ConnectedMaster(
            entry.value
        )
    }

    CurrentSensor(sensorType = sensorType, shouldDisableSensorChange = true)
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
fun ConnectedMaster(name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.connected),
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            text = stringResource(R.string.connected_to_device),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = name,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(8.dp)
        )
    }

}