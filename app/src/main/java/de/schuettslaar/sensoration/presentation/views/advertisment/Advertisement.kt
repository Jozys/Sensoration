package de.schuettslaar.sensoration.presentation.views.advertisment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.core.StatusInformation
import de.schuettslaar.sensoration.presentation.core.data.DataDisplay
import de.schuettslaar.sensoration.presentation.core.sensor.CurrentSensor
import de.schuettslaar.sensoration.presentation.views.connected.ConnectedDevicesPreview
import de.schuettslaar.sensoration.presentation.views.home.HomeAppBar
import de.schuettslaar.sensoration.utils.getStringResourceByName

@Composable
fun Advertisement(onBack: () -> Unit) {

    var viewModel = viewModel<AdvertisementViewModel>()

    Scaffold(
        topBar = {
            AnimatedVisibility(!viewModel.isDrawerOpen.value) { HomeAppBar() }

        }
    ) { innerPadding ->
        AdvertisementContent(
            modifier = Modifier.padding(innerPadding),
            onBack = onBack,
            connectedDevices = viewModel.connectedDevices,
            sensorType = viewModel.currentSensorType,
            status = viewModel.status,
            onDrawerOpen = {
                viewModel.isDrawerOpen.value = true
            },
            onStartReceiving = {
                viewModel.startReceiving()
            },
            onStop = {
                viewModel.stop()
            },
            onSensorChange = {
                viewModel.currentSensorType = it
            },
            onStartDebugMeasurement = {
                viewModel.startDebugMeasurement()
            },
            onStopDebugMeasurement = {
                viewModel.stopDebugMeasurement()
            }
        )
    }
    ConnectedDevicesPreview(
        isDrawerOpen = viewModel.isDrawerOpen,
        connectedDevices = viewModel.connectedDevices,
        connectionDevicesStatus = viewModel.connectionDevicesStatus,
        onConfirmRemove = {
            viewModel.disconnect(it)
        })


}

@Composable
fun AdvertisementContent(
    modifier: Modifier = Modifier,
    connectedDevices: Map<String, String>,
    sensorType: SensorType?,
    status: NearbyStatus,
    onDrawerOpen: () -> Unit,
    onBack: () -> Unit,
    onStartReceiving: () -> Unit,
    onStop: () -> Unit,
    onSensorChange: (SensorType) -> Unit = { },
    onStartDebugMeasurement: () -> Unit = { },
    onStopDebugMeasurement: () -> Unit = { },
) {
    Column(
        modifier = modifier
            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatusInformation(
            statusText = getStringResourceByName(
                context = LocalContext.current,
                resName = status.name
            ).uppercase(),
        )
        ListItem(
            modifier = Modifier.clickable(onClick = {
                onDrawerOpen()
            }),
            headlineContent = {
                Text(stringResource(R.string.connected_devices))
            },
            supportingContent = {
                Text(
                    stringResource(
                        R.string.connected_devices_description,
                        connectedDevices.size
                    )
                )
            },
            trailingContent = {
                if (connectedDevices.isEmpty()) {
                    Icon(Icons.Default.Close, stringResource(R.string.no_connected_devices))
                } else {
                    Icon(Icons.Default.Check, stringResource(R.string.connected_devices))
                }
            }
        )

        CurrentSensor(sensorType = sensorType, shouldDisableSensorChange = false, onSensorChange = {
            onSensorChange(it)
        }, noSensorSelectedDescription = R.string.no_sensor_selected, trailingContent = {
            Button(
                enabled = connectedDevices.isNotEmpty() && sensorType != null,
                onClick = onStartReceiving,
            ) {
                Text(stringResource(R.string.start_receiving_data))
            }
        })

        DataDisplay(sensorType)

        Button(onClick = {
            onStop()
            onBack()
        }) {
            Text(stringResource(R.string.stop_advertising))
        }

        Button(onClick = { onStartDebugMeasurement() }) {
            Text(stringResource(R.string.start_debug_measurement))
        }
        Button(onClick = { onStopDebugMeasurement() }) {
            Text(stringResource(R.string.stop_debug_measurement))
        }
    }
}