package de.schuettslaar.sensoration.views.home

import android.annotation.SuppressLint
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
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.nearby.NearbyStatus
import de.schuettslaar.sensoration.views.advertisment.Advertisement
import de.schuettslaar.sensoration.views.discovering.Discovering

@Composable()
fun HomeView(onBack: () -> Unit) {

    var viewModel = viewModel<HomeViewModel>()

    Scaffold(
        topBar = {
            HomeAppBar()
        }
    ) { innerPadding ->
        HomeContent(
            Modifier.padding(innerPadding),
            viewModel.text,
            viewModel.possibleConnections,
            {
                viewModel.startDiscovering()
            },
            {
                viewModel.stopDiscovering()
            },
            {
                viewModel.startAdvertising()
            },
            {
                viewModel.stopAdvertising()
            },

            {
                viewModel.connect(it)
            },
            viewModel.status
        )
    }

}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    text: String,
    possibleDevices: Map<String, DiscoveredEndpointInfo>,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onStartAdvertising: () -> Unit,
    onStopAdvertising: () -> Unit,
    onDeviceClick: (String) -> Unit,
    status: NearbyStatus
) {

    Column(modifier = modifier.fillMaxSize()) {
        // Debug text to verify the status is changing
        Text(
            text = "Current status: $status",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )
        
        if (status == NearbyStatus.ADVERTISING) {
            Advertisement(onStopAdvertising)
        }

        if (status == NearbyStatus.DISCOVERING) {
            Discovering(possibleDevices, onDeviceClick, onStopDiscovery)
        }

        if (status == NearbyStatus.STOPPED) {
            StartActions(
                onStartDiscovery = onStartDiscovery,
                onStartAdvertising = onStartAdvertising
            )
        }
    }

}

@Composable
fun StartActions(
    modifier: Modifier = Modifier,
    onStartDiscovery: () -> Unit,
    onStartAdvertising: () -> Unit
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
                        painter = painterResource(id = R.drawable.ic_launcher_monochrome),
                        contentDescription = stringResource(R.string.app_icon_desc),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(text = stringResource(R.string.title))
            }
        }

    )
}
