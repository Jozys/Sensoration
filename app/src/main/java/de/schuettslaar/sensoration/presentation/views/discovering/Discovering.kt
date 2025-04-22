package de.schuettslaar.sensoration.presentation.views.discovering

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
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
            Text(
                text = stringResource(R.string.current_status, viewModel.status),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(R.string.discovering_title)
            )
            LazyColumn {
                items(viewModel.possibleConnections.entries.toList()) { (id, info) ->
                    BuildDeviceEntry(id, info, {
                        viewModel.connect(id)
                    }, viewModel.connectedDevices.containsKey(id))
                }
            }

            if (viewModel.connectedDevices.isEmpty()) Button(onClick = {
                viewModel.stop()
                onBack()
            }) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.stop_discovering)
                )
            }

            if (viewModel.connectedDevices.isNotEmpty()) {
                Button(onClick = {
                    viewModel.sendMessage()
                }) {
                    Text(
                        text = stringResource(R.string.debug_send_message)
                    )
                }
                Button(onClick = {
                    viewModel.disconnect()
                }) {
                    Text(
                        text = stringResource(R.string.disconnect_from_device)
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
    checked: Boolean
) {
    ListItem(modifier = Modifier.clickable {
        onClick(id)
    }, trailingContent = {
        if (checked) {
            Icon(Icons.Default.Check, "Connected")
        } else {
            Icon(Icons.Default.Info, "Not Connected")
        }
    }, headlineContent = {
        Text("Device: ${info.endpointName} (ID: $id)")
    })

}