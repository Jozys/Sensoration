package de.schuettslaar.sensoration.presentation.views.connected

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R

@Composable
fun ConnectedList(
    connectedDevices: Map<String, String>?,
    onConfirmRemove: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column {

        Text(
            text = stringResource(R.string.connected_devices),
            textAlign = TextAlign.Center,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
        if (connectedDevices.isNullOrEmpty()) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = "No connected devices",
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = stringResource(R.string.no_connected_devices),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }

        }
        LazyColumn {
            items(connectedDevices?.entries?.toList() ?: emptyList()) { name ->
                ListItem(
                    modifier = Modifier.clickable(onClick = {
                        showDialog = true
                    }),
                    headlineContent = {
                        Text(name.value)
                    },
                    supportingContent = {
                        Text(name.key)
                    },
                    trailingContent = {
                        Icon(Icons.Default.Check, "Client status")
                    }
                )
                if (showDialog) {
                    RemoveDeviceDialog(
                        onDismissRequest = { showDialog = false },
                        onConfirmation = {
                            onConfirmRemove(name.key)
                            showDialog = false
                        },
                        dialogTitle = stringResource(R.string.remove_device),

                        dialogText = stringResource(R.string.remove_device_confirm)
                    )
                }
            }
        }
    }
}