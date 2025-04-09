package de.schuettslaar.sensoration.views.connected

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun ConnectedList(
    connectedDevice: Map<String, String>?,
    onSendMessage: () -> Unit,
    onConfirmRemove: (String) -> Unit,
    onStop: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        LazyColumn {
            items(connectedDevice?.entries?.toList() ?: emptyList()) { name ->
                ListItem(
                    modifier = Modifier.clickable(onClick = {
                        showDialog = true;
                    }),
                    headlineContent = {
                        Text("Connected to $name")
                    },
                    trailingContent = {
                        Icon(Icons.Default.Check, "Client status")
                    }
                )
                if (showDialog) {
                    RemoveDeviceDialog(
                        onDismissRequest = { showDialog = false },
                        onConfirmation = {
                            print("Removing device")
                            onConfirmRemove(name.key)
                            showDialog = false
                        },
                        dialogTitle = "Remove Device",

                        dialogText = "Are you sure you want to remove the device?"
                    )
                }
            }
        }

        Button(onClick = {
            onSendMessage()
        }) {
            Text("Send Message")
        }

        Button(onClick = {
            onStop()
        }) {
            Text("Stop")
        }
    }
}