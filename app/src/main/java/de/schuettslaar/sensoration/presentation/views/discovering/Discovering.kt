package de.schuettslaar.sensoration.presentation.views.discovering

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import de.schuettslaar.sensoration.utils.getStringResourceByName


@Composable
fun Discovering(onBack: () -> Unit) {
    val viewModel = viewModel<DiscoveringViewModel>()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Dynamic spacing based on screen size
    val verticalSpacing = (screenHeight * 0.02f).coerceIn(8.dp, 16.dp)
    val compact = screenHeight < 600.dp || (isLandscape && screenWidth < 700.dp)

    Scaffold(
        topBar = {
            DiscoveryAppBar(onBack = onBack)
        }
    ) { innerPadding ->
        if (viewModel.isLoading) {
            LoadingState(
                status = viewModel.status,
                modifier = Modifier.padding(innerPadding)
            )
        } else if (viewModel.connectedDevices.isEmpty()) {
            DiscoveringState(
                modifier = Modifier.padding(innerPadding),
                connect = { viewModel.connect(it) },
                stop = {
                    viewModel.stop()
                    onBack()
                },
                possibleConnections = viewModel.possibleConnections,
                status = viewModel.status,
                isLandscape = isLandscape,
                compact = compact,
                spacing = verticalSpacing
            )
        } else {
            ConnectedState(
                modifier = Modifier.padding(innerPadding),
                deviceEntry = viewModel.connectedDevices.entries.first(),
                sendMessage = { viewModel.sendMessage() },
                disconnect = { viewModel.disconnect() },
                deviceStatus = viewModel.thisApplicationStatus,
                sensorType = viewModel.currentSensorType,
                isLandscape = isLandscape,
                compact = compact,
                spacing = verticalSpacing
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.discovery_title),
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
        }
    )
}

@Composable
fun LoadingState(status: NearbyStatus, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StatusInformation(
            statusText = getStringResourceByName(
                context = LocalContext.current,
                resName = status.name
            ).uppercase()
        )

        CircularProgressIndicator(
            modifier = Modifier
                .padding(24.dp)
                .size(64.dp),
            strokeWidth = 6.dp
        )

        Text(
            text = stringResource(R.string.searching_devices),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun EmptyDeviceList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(R.string.no_devices_found),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun DeviceListItem(
    id: DeviceId,
    info: DiscoveredEndpointInfo,
    onClick: (id: DeviceId) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick(id) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Devices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = info.endpointName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.device_id_format, id.name),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.connect_button_description),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DiscoveringState(
    modifier: Modifier = Modifier,
    connect: (DeviceId) -> Unit,
    stop: () -> Unit,
    possibleConnections: Map<DeviceId, DiscoveredEndpointInfo>,
    status: NearbyStatus,
    isLandscape: Boolean,
    compact: Boolean,
    spacing: Dp
) {
    val scrollState = rememberScrollState()
    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(if (!isLandscape) Modifier.verticalScroll(scrollState) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatusInformation(
            statusText = getStringResourceByName(
                context = LocalContext.current,
                resName = status.name
            ).uppercase(),
            modifier = Modifier.padding(top = spacing, bottom = spacing / 2)
        )

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left column: Device list
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.8f),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.available_connections),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )

                        if (possibleConnections.isEmpty()) {
                            EmptyDeviceList()
                        } else {
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.weight(1f)
                            ) {
                                items(possibleConnections.entries.toList()) { (id, info) ->
                                    DeviceListItem(id, info, connect)
                                }
                            }
                        }
                    }
                }

                // Right column: Instructions
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.8f),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            modifier = Modifier
                                .size(if (compact) 48.dp else 64.dp)
                                .padding(8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = stringResource(R.string.discovering_description),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Button(
                            onClick = stop,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.stop_discovering))
                        }
                    }
                }
            }
        } else {
            // Portrait layout
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = spacing),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.available_connections),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (possibleConnections.isEmpty()) {
                        EmptyDeviceList()
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.height(240.dp)
                        ) {
                            items(possibleConnections.entries.toList()) { (id, info) ->
                                DeviceListItem(id, info, connect)
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        modifier = Modifier
                            .size(if (compact) 48.dp else 64.dp)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.discovering_description),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Button(
                        onClick = stop,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.stop_discovering))
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectedState(
    modifier: Modifier = Modifier,
    deviceEntry: Map.Entry<DeviceId, String>,
    sendMessage: () -> Unit,
    disconnect: () -> Unit,
    sensorType: SensorType? = null,
    deviceStatus: ApplicationStatus,
    isLandscape: Boolean,
    compact: Boolean,
    spacing: Dp
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(Modifier.verticalScroll(scrollState)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left column: Connected device info
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.8f),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ConnectedDeviceContent(deviceName = deviceEntry.value, compact = compact)

                        Spacer(modifier = Modifier.height(16.dp))

                        // TODO: either remove or add useful functionality
                        Button(
                            onClick = sendMessage,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.send_test_message))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = disconnect,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(stringResource(R.string.disconnect_device))
                        }
                    }
                }

                // Right column: Sensor info
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.8f),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StatusInformation(
                            statusText = deviceStatus.name,
                            modifier = Modifier.padding(top = spacing, bottom = spacing / 2)
                        )

                        Text(
                            text = stringResource(R.string.sensor_information),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        CurrentSensor(
                            sensorType = sensorType,
                            shouldDisableSensorChange = true
                        )
                    }
                }
            }
        } else {
            StatusInformation(
                statusText = deviceStatus.name,
                modifier = Modifier.padding(top = spacing, bottom = spacing / 2)
            )
            // Portrait layout: Stacked cards
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = spacing),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ConnectedDeviceContent(deviceName = deviceEntry.value, compact = compact)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = sendMessage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.send_test_message))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = disconnect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(stringResource(R.string.disconnect_device))
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.sensor_information),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    CurrentSensor(
                        sensorType = sensorType,
                        shouldDisableSensorChange = true
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectedDeviceContent(deviceName: String, compact: Boolean) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = stringResource(R.string.connected),
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .size(if (compact) 48.dp else 64.dp)
            .padding(8.dp)
    )

    Text(
        text = stringResource(R.string.connected_to_device),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Text(
        text = deviceName,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}