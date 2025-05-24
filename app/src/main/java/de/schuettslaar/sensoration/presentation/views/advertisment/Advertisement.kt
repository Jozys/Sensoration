package de.schuettslaar.sensoration.presentation.views.advertisment

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.core.StatusInformation
import de.schuettslaar.sensoration.presentation.core.data.DataDisplay
import de.schuettslaar.sensoration.presentation.core.sensor.CurrentSensor
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.utils.getStringResourceByName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Advertisement(onBack: () -> Unit) {
    val masterViewModel = viewModel<MasterViewModel>()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val compact = screenHeight < 600.dp || (isLandscape && screenWidth < 600.dp)
    val contentScrollState = rememberScrollState()

    // Set the drawer open state based on ViewModel
    masterViewModel.isDrawerOpen.value = drawerState.isOpen

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ConnectedDevicesPreview(
                    connectedDevices = masterViewModel.connectedDeviceInfos,
                    onDisconnect = { masterViewModel.disconnect(it) },
                    onConfirmRemove = { /* Handle device removal confirm */ },
                    isDrawerOpen = drawerState.isOpen
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                AdvertisementAppBar(
                    onBack = onBack,
                    onOpenConnectedDevices = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    deviceCount = masterViewModel.connectedDeviceInfos.size
                )
            }
        ) { innerPadding ->
            if (isLandscape) {
                LandscapeLayout(
                    innerPadding = innerPadding,
                    masterViewModel = masterViewModel,
                    compact = compact,
                    contentScrollState = contentScrollState
                )
            } else {
                PortraitLayout(
                    innerPadding = innerPadding,
                    masterViewModel = masterViewModel,
                    compact = compact,
                    contentScrollState = contentScrollState
                )
            }
        }
    }
}

@Composable
fun LandscapeLayout(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    masterViewModel: MasterViewModel,
    compact: Boolean,
    contentScrollState: androidx.compose.foundation.ScrollState
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {


        // Scrollable content area
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(contentScrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status and device info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusInformation(
                    statusText = getStringResourceByName(
                        LocalContext.current,
                        masterViewModel.status.name
                    ).uppercase()
                )

                ConnectedDevicesInfo(
                    deviceCount = masterViewModel.connectedDeviceInfos.size,
                    onViewDevices = {
                        masterViewModel.isDrawerOpen.value = true
                    },
                    compact = compact
                )

                // Static control bar at the top
                SensorSelectionCard(
                    sensorType = masterViewModel.currentSensorType,
                    onSensorSelected = { masterViewModel.currentSensorType = it },
                    isReceiving = masterViewModel.isReceiving,
                    onStartReceiving = { masterViewModel.startReceiving() },
                    onStopReceiving = { masterViewModel.stopReceiving() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    compact = compact
                )
            }

            // Data visualization
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                if (masterViewModel.isReceiving) {
                    DataDisplay(
                        sensorType = masterViewModel.currentSensorType,
                        devices = masterViewModel.connectedDeviceInfos,
                        timeBuckets = masterViewModel.synchronizedData,
                        activeDevices = masterViewModel.getActiveDevices()
                    )
                } else {
                    NoMeasurementInfo()
                }
            }
        }
    }
}

@Composable
fun PortraitLayout(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    masterViewModel: MasterViewModel,
    compact: Boolean,
    contentScrollState: androidx.compose.foundation.ScrollState
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        StatusInformation(
            statusText = getStringResourceByName(
                LocalContext.current,
                masterViewModel.status.name
            ).uppercase(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        ConnectedDevicesInfo(
            deviceCount = masterViewModel.connectedDeviceInfos.size,
            onViewDevices = {
                masterViewModel.isDrawerOpen.value = true
            },
            compact = compact,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Static control bar at the top
        SensorSelectionCard(
            sensorType = masterViewModel.currentSensorType,
            onSensorSelected = { masterViewModel.currentSensorType = it },
            isReceiving = masterViewModel.isReceiving,
            onStartReceiving = { masterViewModel.startReceiving() },
            onStopReceiving = { masterViewModel.stopReceiving() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            compact = compact
        )


        // Scrollable data area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(contentScrollState)
        ) {
            if (masterViewModel.isReceiving) {
                DataDisplay(
                    sensorType = masterViewModel.currentSensorType,
                    devices = masterViewModel.connectedDeviceInfos,
                    timeBuckets = masterViewModel.synchronizedData,
                    activeDevices = masterViewModel.getActiveDevices()
                )
            } else {
                NoMeasurementInfo()
            }
        }
    }
}

@Composable
fun NoMeasurementInfo() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_sensor_selected),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorPicker(
    currentSensorType: SensorType?,
    onSensorSelected: (SensorType) -> Unit,
    isDisabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val sensorTypes = SensorType.values()
    val context = LocalContext.current

    ExposedDropdownMenuBox(
        expanded = expanded && !isDisabled,
        onExpandedChange = { if (!isDisabled) expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = currentSensorType?.let { getStringResourceByName(context, it.name) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.current_sensor)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            enabled = !isDisabled,
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sensorTypes.forEach { sensor ->
                DropdownMenuItem(
                    text = {
                        Text(getStringResourceByName(context, sensor.name))
                    },
                    onClick = {
                        onSensorSelected(sensor)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SensorSelectionCard(
    sensorType: SensorType?,
    onSensorSelected: (SensorType) -> Unit,
    isReceiving: Boolean,
    onStartReceiving: () -> Unit,
    onStopReceiving: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val masterViewModel = viewModel<MasterViewModel>()

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (compact) {
                // Compact layout - controls side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CurrentSensor(
                        sensorType = sensorType,
                        shouldDisableSensorChange = isReceiving,
                        onSensorChange = onSensorSelected,
//                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Main device sensor data switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Switch(
                            checked = masterViewModel.masterProvidesData,
                            onCheckedChange = { masterViewModel.toggleMasterProvidesData() },
                            enabled = !isReceiving
                        )

                        Text(
                            text = "Main device sensors",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    MeasurementButton(
                        isReceiving = isReceiving,
                        onStartReceiving = onStartReceiving,
                        onStopReceiving = onStopReceiving,
                        isEnabled = sensorType != null,
                        modifier = Modifier.width(120.dp)
                    )
                }
            } else {
                // Standard layout - stacked controls
                Text(
                    text = stringResource(R.string.sensor_information),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CurrentSensor(
                    sensorType = sensorType,
                    onSensorChange = onSensorSelected,
                    shouldDisableSensorChange = isReceiving,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Add the switch for main device data collection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.collect_main_device_data),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Switch(
                        checked = masterViewModel.masterProvidesData,
                        onCheckedChange = { masterViewModel.toggleMasterProvidesData() },
                        enabled = !isReceiving
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                MeasurementButton(
                    isReceiving = isReceiving,
                    onStartReceiving = onStartReceiving,
                    onStopReceiving = onStopReceiving,
                    isEnabled = sensorType != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MeasurementButton(
    isReceiving: Boolean,
    onStartReceiving: () -> Unit,
    onStopReceiving: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = if (isReceiving) onStopReceiving else onStartReceiving,
        enabled = isEnabled,
        colors = if (isReceiving) ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) else ButtonDefaults.buttonColors(),
        modifier = modifier
    ) {
        Text(
            text = stringResource(
                if (isReceiving) R.string.stop_receiving_data
                else R.string.start_receiving_data
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementAppBar(
    onBack: () -> Unit,
    onOpenConnectedDevices: () -> Unit,
    deviceCount: Int
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.advertising_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button_description)
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenConnectedDevices) {
                Icon(
                    Icons.Default.Devices,
                    contentDescription = stringResource(R.string.connected_devices)
                )
            }
        }
    )
}

@Composable
fun ConnectedDevicesInfo(
    deviceCount: Int,
    onViewDevices: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (compact) 24.dp else 32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.connected_devices),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = stringResource(R.string.connected_devices_description, deviceCount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = onViewDevices,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(text = "View")
                }
            }
        }
    }
}

@Composable
fun ConnectedDevicesPreview(
    connectedDevices: Map<DeviceId, DeviceInfo>,
    onDisconnect: (DeviceId) -> Unit,
    onConfirmRemove: () -> Unit,
    isDrawerOpen: Boolean
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.connected_devices),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (connectedDevices.isEmpty()) {
            Text(
                text = stringResource(R.string.no_connected_devices),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else {
            // Connected devices list
            connectedDevices.forEach { (deviceId, deviceInfo) ->
                DeviceListItem(
                    deviceId = deviceId,
                    deviceInfo = deviceInfo,
                    onDisconnect = { onDisconnect(deviceId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DeviceListItem(
    deviceId: DeviceId,
    deviceInfo: DeviceInfo,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = deviceInfo.deviceName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Status: ${deviceInfo.applicationStatus}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "ID: ${deviceId.name}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = onDisconnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = stringResource(R.string.disconnect_device))
            }
        }
    }
}