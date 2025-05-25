package de.schuettslaar.sensoration.presentation.views.devices.main.advertisment

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.utils.getStringResourceByName
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDeviceScreen(onBack: () -> Unit) {
    val mainDeviceViewModel = viewModel<MainDeviceViewModel>()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val compact = screenHeight < 600.dp || (isLandscape && screenWidth < 600.dp)
    val contentScrollState = rememberScrollState()

    // Set the drawer open state based on ViewModel
    mainDeviceViewModel.isDrawerOpen.value = drawerState.isOpen

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                ConnectedDevicesPreview(
                    connectedDevices = mainDeviceViewModel.connectedDeviceInfos,
                    onDisconnect = { mainDeviceViewModel.disconnect(it) },
                    onConfirmRemove = { /* Handle device removal confirm */ },
                    onSendTestMessage = { deviceId -> mainDeviceViewModel.sendTestMessage(deviceId) },
                    isDrawerOpen = drawerState.isOpen
                )
            }
        }) {
        Scaffold(
            topBar = {
                AdvertisementAppBar(
                    onBack = onBack, onOpenConnectedDevices = {
                        scope.launch {
                            drawerState.open()
                        }
                    }, deviceCount = mainDeviceViewModel.connectedDeviceInfos.size
                )
            }) { innerPadding ->
            if (isLandscape) {
                LandscapeLayout(
                    innerPadding = innerPadding,
                    mainDeviceViewModel = mainDeviceViewModel,
                    compact = compact,
                    contentScrollState = contentScrollState
                )
            } else {
                PortraitLayout(
                    innerPadding = innerPadding,
                    mainDeviceViewModel = mainDeviceViewModel,
                    compact = compact,
                    contentScrollState = contentScrollState
                )
            }
        }
    }
}

@Composable
fun LandscapeLayout(
    innerPadding: PaddingValues,
    mainDeviceViewModel: MainDeviceViewModel,
    compact: Boolean,
    contentScrollState: ScrollState
) {
    var isMetadataPanelExpanded by remember { mutableStateOf(true) }
    val metadataPanelScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        val expandedPanelWidth =
            if (isMetadataPanelExpanded && compact) 350.dp else if (isMetadataPanelExpanded) 400.dp else 56.dp
        Row(modifier = Modifier.fillMaxSize()) {
            // Metadata panel with fixed width
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(expandedPanelWidth)
                    .animateContentSize(
                        // Smoother animation to prevent layout issues
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                tonalElevation = 1.dp
            ) {
                if (isMetadataPanelExpanded) {
                    // Expanded view
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header with status and collapse button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusInformation(
                                statusText = getStringResourceByName(
                                    LocalContext.current, mainDeviceViewModel.status.name
                                ).uppercase()
                            )
                            // Collapse button
                            IconButton(
                                onClick = { isMetadataPanelExpanded = false },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Collapse panel"
                                )
                            }
                        }

                        // Make the content scrollable
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .verticalScroll(metadataPanelScrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Content
                            ConnectedDevicesInfo(
                                deviceCount = mainDeviceViewModel.connectedDeviceInfos.size,
                                onViewDevices = { mainDeviceViewModel.isDrawerOpen.value = true },
                                compact = compact
                            )

                            SensorSelectionCard(
                                sensorType = mainDeviceViewModel.currentSensorType,
                                onSensorSelected = { mainDeviceViewModel.currentSensorType = it },
                                isReceiving = mainDeviceViewModel.isReceiving,
                                onStartReceiving = { mainDeviceViewModel.startReceiving() },
                                onStopReceiving = { mainDeviceViewModel.stopReceiving() },
                                modifier = Modifier.fillMaxWidth(),
                                compact = compact
                            )
                        }
                    }
                } else {
                    // Collapsed view - just the expand button
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 16.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = { isMetadataPanelExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Expand panel"
                            )
                        }
                    }
                }
            }

            // Data display area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(contentScrollState)
                ) {
                    if (mainDeviceViewModel.isReceiving) {
                        DataDisplay(
                            sensorType = mainDeviceViewModel.currentSensorType,
                            devices = mainDeviceViewModel.connectedDeviceInfos,
                            timeBuckets = mainDeviceViewModel.synchronizedData,
                            activeDevices = mainDeviceViewModel.getActiveDevices()
                        )
                    } else {
                        NoMeasurementInfo()
                    }
                }
            }
        }
    }
}

@Composable
fun PortraitLayout(
    innerPadding: PaddingValues,
    mainDeviceViewModel: MainDeviceViewModel,
    compact: Boolean,
    contentScrollState: ScrollState
) {
    var isMetadataPanelExpanded by remember { mutableStateOf(true) }
    val metadataPanelScrollState = rememberScrollState()
    val expandedPanelHeight =
        if (isMetadataPanelExpanded && compact) 350.dp
        else if (isMetadataPanelExpanded) 400.dp
        else 80.dp

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(expandedPanelHeight)
                .animateContentSize(
                    // Smoother animation to prevent layout issues
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = Spring.StiffnessLow
                    )
                ),
            tonalElevation = 1.dp
        ) {
            if (isMetadataPanelExpanded) {
                // Expanded view
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with status and collapse button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusInformation(
                            statusText = getStringResourceByName(
                                LocalContext.current, mainDeviceViewModel.status.name
                            ).uppercase(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        // Collapse button
                        IconButton(
                            onClick = { isMetadataPanelExpanded = false },
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Collapse panel"
                            )
                        }
                    }

                    // Make the content scrollable
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .verticalScroll(metadataPanelScrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Content
                        ConnectedDevicesInfo(
                            deviceCount = mainDeviceViewModel.connectedDeviceInfos.size,
                            onViewDevices = {
                                mainDeviceViewModel.isDrawerOpen.value = true
                            },
                            compact = compact,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // Static control bar at the top
                        SensorSelectionCard(
                            sensorType = mainDeviceViewModel.currentSensorType,
                            onSensorSelected = { mainDeviceViewModel.currentSensorType = it },
                            isReceiving = mainDeviceViewModel.isReceiving,
                            onStartReceiving = { mainDeviceViewModel.startReceiving() },
                            onStopReceiving = { mainDeviceViewModel.stopReceiving() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            compact = compact
                        )
                    }
                }
            } else {
                // Collapsed view - just the expand button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusInformation(
                        statusText = getStringResourceByName(
                            LocalContext.current, mainDeviceViewModel.status.name
                        ).uppercase(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    // Collapse button
                    IconButton(
                        onClick = { isMetadataPanelExpanded = true },
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand panel"
                        )
                    }
                }

            }
        }


        // Data display area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(contentScrollState)
        ) {
            if (mainDeviceViewModel.isReceiving) {
                DataDisplay(
                    sensorType = mainDeviceViewModel.currentSensorType,
                    devices = mainDeviceViewModel.connectedDeviceInfos,
                    timeBuckets = mainDeviceViewModel.synchronizedData,
                    activeDevices = mainDeviceViewModel.getActiveDevices()
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
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
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
    val mainDeviceViewModel = viewModel<MainDeviceViewModel>()

    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (compact) {
                // Compact layout - controls side by side
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CurrentSensor(
                            sensorType = sensorType,
                            shouldDisableSensorChange = isReceiving,
                            onSensorChange = onSensorSelected,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Main device sensor data switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.collect_main_device_data),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Switch(
                        checked = mainDeviceViewModel.mainDeviceIsProvidingData,
                        onCheckedChange = { mainDeviceViewModel.toggleMainDeviceProvidingData() },
                        enabled = !isReceiving
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

            } else {
                // Standard layout - stacked controls
                Text(
                    text = stringResource(R.string.sensor_information),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CurrentSensor(
                            sensorType = sensorType,
                            shouldDisableSensorChange = isReceiving,
                            onSensorChange = onSensorSelected,
                        )
                    }
                }

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
                        checked = mainDeviceViewModel.mainDeviceIsProvidingData,
                        onCheckedChange = { mainDeviceViewModel.toggleMainDeviceProvidingData() },
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
    onBack: () -> Unit, onOpenConnectedDevices: () -> Unit, deviceCount: Int
) {
    TopAppBar(title = {
        Text(
            text = stringResource(R.string.advertising_title),
            style = MaterialTheme.typography.titleMedium
        )
    }, navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_button_description)
            )
        }
    }, actions = {
        IconButton(onClick = onOpenConnectedDevices) {
            Icon(
                Icons.Default.Devices,
                contentDescription = stringResource(R.string.connected_devices)
            )
        }
    })
}

@Composable
fun ConnectedDevicesInfo(
    deviceCount: Int, onViewDevices: () -> Unit, compact: Boolean, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
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
                    onClick = onViewDevices, modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(text = "View") // TODO: Localize this string
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
    onSendTestMessage: (DeviceId) -> Unit,
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
                    onDisconnect = { onDisconnect(deviceId) },
                    onSendTestMessage = { onSendTestMessage(deviceId) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DeviceListItem(
    deviceId: DeviceId,
    deviceInfo: DeviceInfo,
    onDisconnect: () -> Unit,
    onSendTestMessage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = deviceInfo.deviceName, style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Status: ${deviceInfo.applicationStatus}", // TODO: Localize this string
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "ID: ${deviceId.name}", // TODO: Localize this string
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onSendTestMessage, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(text = stringResource(R.string.send_test_message))
                }

                Button(
                    onClick = onDisconnect, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = stringResource(R.string.disconnect_device))
                }
            }
        }
    }
}
