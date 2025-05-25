package de.schuettslaar.sensoration.presentation.views.connected

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.presentation.core.drawerOverlay.DrawerOverlay
import de.schuettslaar.sensoration.presentation.core.icon.ApplicationStatusIcon
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model.DeviceInfo

@Composable
fun ConnectedDevicesPreview(
    connectedDeviceInfos: Map<DeviceId, DeviceInfo>?,
    onConfirmRemove: (DeviceId) -> Unit,
    isDrawerOpen: MutableState<Boolean>,

    ) {
    AnimatedVisibility(
        visible = isDrawerOpen.value,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        enter = slideInHorizontally(
            animationSpec = tween(
                durationMillis = 200,
                easing = LinearEasing
            )
        ) { fullWidth ->
            fullWidth
        } + fadeIn(
            animationSpec = tween(durationMillis = 200)
        ),
        exit = slideOutHorizontally(
            animationSpec = tween(
                durationMillis = 200,
                easing = LinearEasing
            )
        ) { fullWidth ->
            fullWidth
        } + fadeOut(
            animationSpec = tween(durationMillis = 200)
        )
    ) {
        DrawerOverlay(
            modifier = Modifier.padding(8.dp),
            title = {
                Text(
                    text = stringResource(R.string.connected_devices),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(8.dp)
                )
            },
            drawerContent = {
                ConnectedList(
                    connectedDeviceInfos,
                    onConfirmRemove = onConfirmRemove
                )
            },
            isDrawerOpen = isDrawerOpen.value,
            onDismiss = {
                isDrawerOpen.value = false
            },

            )
    }

}

@Composable
fun ConnectedList(
    connectedDeviceInfos: Map<DeviceId, DeviceInfo>?,
    onConfirmRemove: (DeviceId) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        if (connectedDeviceInfos.isNullOrEmpty()) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.no_connected_devices),
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = stringResource(R.string.no_connected_devices),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }

        }
        LazyColumn {
            items(connectedDeviceInfos?.entries?.toList() ?: emptyList()) { value ->
                ListItem(
                    modifier = Modifier.clickable(onClick = {
                        showDialog = true
                    }),
                    headlineContent = {
                        Text(value.value.deviceName)
                    },
                    supportingContent = {
                        Text(value.key.name)
                    },
                    trailingContent = {
                        ApplicationStatusIcon(
                            applicationStatus = connectedDeviceInfos?.get(value.key)
                                ?.applicationStatus
                                ?: ApplicationStatus.ERROR,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                )
                if (showDialog) {
                    RemoveDeviceDialog(
                        onDismissRequest = { showDialog = false },
                        onConfirmation = {
                            onConfirmRemove(value.key)
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