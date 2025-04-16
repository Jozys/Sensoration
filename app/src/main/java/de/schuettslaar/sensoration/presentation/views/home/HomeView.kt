package de.schuettslaar.sensoration.presentation.views.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.domain.Client
import de.schuettslaar.sensoration.domain.Device
import de.schuettslaar.sensoration.domain.Master
import de.schuettslaar.sensoration.presentation.views.advertisment.Advertisement
import de.schuettslaar.sensoration.presentation.views.connected.ConnectedList
import de.schuettslaar.sensoration.presentation.views.discovering.Discovering
import de.schuettslaar.sensoration.views.home.HomeViewModel
import java.util.logging.Logger

@Composable()
fun HomeView(onBack: () -> Unit) {

    var viewModel = viewModel<HomeViewModel>()

    Scaffold(
        topBar = {
            HomeAppBar()
        }
    ) { innerPadding ->
        HomeContent(
            context = LocalContext.current,
            Modifier.padding(innerPadding),
            viewModel.text,
            viewModel.possibleConnections,
            onStart = { it ->
                viewModel.start(it)
            },
            onStop = {
                viewModel.stop()
            },
            onDeviceClick = {
                viewModel.connect(it)
            },
            onSendMessage = {
                viewModel.sendMessage()
            },
            connectedDevices =
                viewModel.connectedDevices,
            connectedId =
                viewModel.connectedId,
            status = viewModel.status,
            onPossibleDeviceAdd = {
                viewModel.possibleConnections =
                    viewModel.possibleConnections.plus(Pair(it.first, it.second))
            },
            onPossibleDeviceRemove = {
                viewModel.possibleConnections =
                    viewModel.possibleConnections.minus(it)
            },
            onConnectionResultCallback = { endpointId, connectionStatus, status ->
                Logger.getLogger("HomeView").info {
                    "Connected to $endpointId"
                }
                if (connectionStatus.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                    viewModel.connectedId = endpointId

                } else {
                    if (viewModel.connectedDevices.containsKey(endpointId)) {
                        viewModel.connectedDevices = viewModel.connectedDevices.minus(endpointId)
                    }
                    Logger.getLogger("HomeView").info {
                        "Connection failed with status code: ${connectionStatus.status.statusCode}"
                    }
                }
                viewModel.status = status
            },
            onDisconnectedCallback = { endpointId, status ->
                Logger.getLogger("HomeView").info {
                    "Disconnected from $endpointId"
                }
                viewModel.connectedDevices.minus(endpointId)
                viewModel.connectedId = ""
                viewModel.status = status
            },
            onConnectionInitiatedCallback = { endpointId, result ->
                viewModel.connectedDevices =
                    viewModel.connectedDevices.plus(endpointId to result.endpointName)
            }
        )
    }

}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun HomeContent(
    context: Context,
    modifier: Modifier = Modifier,
    text: String,
    possibleDevices: Map<String, DiscoveredEndpointInfo>,
    onPossibleDeviceAdd: (Pair<String, DiscoveredEndpointInfo>) -> Unit = {},
    onPossibleDeviceRemove: (String) -> Unit = {},
    onConnectionResultCallback: (String, ConnectionResolution, NearbyStatus) -> Unit,
    onDisconnectedCallback: (String, NearbyStatus) -> Unit,
    onConnectionInitiatedCallback: (String, ConnectionInfo) -> Unit,
    onStart: (Device) -> Unit,
    onStop: () -> Unit,
    onDeviceClick: (String) -> Unit,
    onSendMessage: () -> Unit,
    connectedDevices: Map<String, String>? = null,
    connectedId: String,
    status: NearbyStatus
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (connectedDevices != null && connectedDevices.isNotEmpty()) {
            ConnectedList(connectedDevice = connectedDevices, onConfirmRemove = {
                onDeviceClick(it)
            }, onSendMessage = {
                onSendMessage()
            }, onStop = {
                onStop()
            })
        } else {
            Text(
                text = "Current status: $status",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )

            if (status == NearbyStatus.ADVERTISING) {
                Advertisement(onStop)
            }

            if (status == NearbyStatus.DISCOVERING) {
                Discovering(possibleDevices, connectedId, onDeviceClick, onStop)
            }
            if (status == NearbyStatus.STOPPED) {
                StartActions(
                    onStartDiscovery = {
                        onStart(
                            Client(
                                context = context,
                                onEndpointAddCallback = onPossibleDeviceAdd,
                                onEndpointRemoveCallback = onPossibleDeviceRemove,
                                onConnectionResultCallback = onConnectionResultCallback,
                                onDisconnectedCallback = onDisconnectedCallback,
                                onConnectionInitiatedCallback = onConnectionInitiatedCallback,

                                )
                        )
                    },
                    onSendMessage = onSendMessage,
                    modifier = Modifier,
                    onStartAdvertising = {
                        onStart(
                            Master(
                                context = context,
                                onConnectionResultCallback = onConnectionResultCallback,
                                onDisconnectedCallback = onDisconnectedCallback,
                                onConnectionInitiatedCallback = onConnectionInitiatedCallback,

                                )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun StartActions(
    modifier: Modifier = Modifier,
    onStartDiscovery: () -> Unit,
    onStartAdvertising: () -> Unit,
    onSendMessage: () -> Unit
) {
    Column(modifier = modifier) {
        Button(onClick = {
            onStartDiscovery()
        }) {
            Text(stringResource(R.string.start_discovery))
        }
        Button(onClick = {
            onStartAdvertising()
        }) {
            Text(stringResource(R.string.start_advertising))
        }


    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar() {
    val context = LocalContext.current
    val appInfo = remember {
        try {
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                appInfo?.let {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = stringResource(R.string.app_icon_desc),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(text = stringResource(R.string.title))
            }
        }

    )
}
