package de.schuettslaar.sensoration.presentation.views.discovering

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.R

@Composable
fun Discovering(
    possibleDevices: Map<String, DiscoveredEndpointInfo>,
    connectedId: String,
    onDeviceClick: (String) -> Unit,
    onDiscoveryStopped: () -> Unit
) {

    Column {
        Text(stringResource(R.string.discovering_title))
        LazyColumn {
            items(possibleDevices.entries.toList()) { (id, info) ->
                BuildDeviceEntry(id, info, onDeviceClick, id == connectedId)
            }
        }
        Button(onClick = {
            onDiscoveryStopped()
        }) {
            Text(stringResource(R.string.stop_discovering))
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
        // TODO: Extract string resources
        Text("Device: ${info.endpointName} (ID: $id)")
    })

}