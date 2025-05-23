package de.schuettslaar.sensoration.presentation.views.advertisment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.core.StatusInformation
import de.schuettslaar.sensoration.presentation.core.data.DataDisplay
import de.schuettslaar.sensoration.presentation.core.sensor.CurrentSensor
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.presentation.views.connected.ConnectedDevicesPreview
import de.schuettslaar.sensoration.presentation.views.home.HomeAppBar
import de.schuettslaar.sensoration.utils.getStringResourceByName

@Composable
fun Advertisement(onBack: () -> Unit) {

    var masterViewModel = viewModel<MasterViewModel>()


    Scaffold(
        topBar = {
            AnimatedVisibility(!masterViewModel.isDrawerOpen.value) {
                HomeAppBar(onSettings = { /* Not required for this screen */ })
            }
        }
    ) { innerPadding ->
        AdvertisementContent(
            modifier = Modifier.padding(innerPadding),
            onBack = onBack,
            connectedDeviceInfos = masterViewModel.connectedDeviceInfos.toMutableMap(),
            sensorType = masterViewModel.currentSensorType,
            status = masterViewModel.status,
            onDrawerOpen = {
                masterViewModel.isDrawerOpen.value = true
            },
            onStartReceiving = {
                masterViewModel.startReceiving()
            },
            onStopReceiving = {
                masterViewModel.stopReceiving()
            },
            isReceiving = masterViewModel.isReceiving,
            onStop = {
                masterViewModel.stop()
            },
            onSensorChange = {
                masterViewModel.currentSensorType = it
            },
            measurementData = masterViewModel.synchronizedData,
            activeDevices = masterViewModel.getActiveDevices(),
        )
    }
    ConnectedDevicesPreview(
        isDrawerOpen = masterViewModel.isDrawerOpen,
        connectedDeviceInfos = masterViewModel.connectedDeviceInfos,
        onConfirmRemove = {
            masterViewModel.disconnect(it)
        })
}

@Composable
fun AdvertisementContent(
    modifier: Modifier = Modifier,
    connectedDeviceInfos: MutableMap<DeviceId, DeviceInfo>,
    sensorType: SensorType?,
    status: NearbyStatus,
    onDrawerOpen: () -> Unit,
    onBack: () -> Unit,
    onStartReceiving: () -> Unit,
    onStopReceiving: () -> Unit,
    isReceiving: Boolean = false,
    onStop: () -> Unit,
    onSensorChange: (SensorType) -> Unit = { },
    measurementData: List<TimeBucket>,
    activeDevices: List<DeviceId>,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
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
                        connectedDeviceInfos.size
                    )
                )
            },
            trailingContent = {
                if (connectedDeviceInfos.isEmpty()) {
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
                enabled = connectedDeviceInfos.isNotEmpty() && sensorType != null,
                onClick = {
                    if (isReceiving) {
                        onStopReceiving()
                    } else {
                        onStartReceiving()
                    }
                },
            ) {
                if (isReceiving) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = stringResource(R.string.stop_receiving_data)
                    )
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.stop_receiving_data)
                    )
                }
            }
        })

        AnimatedVisibility(sensorType != null && isReceiving) {
            DataDisplay(
                sensorType,
                devices = connectedDeviceInfos,
                timeBuckets = measurementData,
                activeDevices = activeDevices,
            )
        }

        Button(onClick = {
            onStop()
            onBack()
        }) {
            Text(stringResource(R.string.stop_advertising))
        }
    }
}