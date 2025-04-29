package de.schuettslaar.sensoration.presentation.views.home

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.presentation.core.Accordion

@Composable()
fun HomeView(onAdvertising: () -> Unit, onDiscovering: () -> Unit, onSettings: () -> Unit) {

    Scaffold(
        topBar = {
            HomeAppBar(onSettings = onSettings)
        }
    ) { innerPadding ->
        HomeContent(
            modifier = Modifier.padding(innerPadding),
            onDiscovery = onDiscovering,
            onAdvertising = onAdvertising
        )
    }

}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    onDiscovery: () -> Unit,
    onAdvertising: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.welcome),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        )
        AppDescription()
        StartActions(
            onStartDiscovery = onDiscovery,
            modifier = Modifier,
            onStartAdvertising = onAdvertising
        )
    }
}

@Composable
fun AppDescription() {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.secondary
    ) {
        Column() {
            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(8.dp),
            )
            Accordion(
                title = stringResource(R.string.discovering_title),
                {
                    Text(
                        text = stringResource(R.string.discovering_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(8.dp),
                    )
                },
            )
            Accordion(
                title = stringResource(R.string.advertising_title),
                {
                    Text(
                        text = stringResource(R.string.advertising_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(8.dp),
                    )
                },
            )


        }
    }

}

@Composable
fun StartActions(
    modifier: Modifier = Modifier,
    onStartDiscovery: () -> Unit,
    onStartAdvertising: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
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
fun HomeAppBar(onSettings: () -> Unit = {}) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                appInfo?.let {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = stringResource(R.string.app_icon_desc),

                        )
                }
                Text(
                    text = stringResource(R.string.title), textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(onClick = {
                        onSettings()
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_desc),
                            modifier = Modifier
                                .padding(end = 12.dp),
                        )
                    }
                }
            }
        }

    )
}
