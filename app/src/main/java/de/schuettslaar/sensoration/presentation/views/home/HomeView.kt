package de.schuettslaar.sensoration.presentation.views.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Sensors
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.presentation.core.animation.NetworkConceptAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(onAdvertising: () -> Unit, onDiscovering: () -> Unit, onSettings: () -> Unit) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Dynamic spacing based on screen size
    val verticalSpacing = (screenHeight * 0.02f).coerceIn(8.dp, 16.dp)
    val horizontalPadding = (screenWidth * 0.04f).coerceIn(12.dp, 24.dp)

    // Calculate if we have enough space to show the welcome text
    val showWelcomeText = screenHeight > 200.dp
    val showDetailedWelcomeText = screenHeight > 500.dp

    // Animation visibility state
    var showConceptAnimation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HomeAppBar(onSettings = onSettings)
        }) { innerPadding ->
        // Wrap everything in a Box to have proper Composable context
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .then(if (!isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isLandscape && showDetailedWelcomeText) Arrangement.Center else Arrangement.Top
            ) {
                if (showWelcomeText || showDetailedWelcomeText) {
                    Text(
                        text = stringResource(R.string.welcome),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(
                            top = verticalSpacing, bottom = verticalSpacing / 2
                        )
                    )

                    if (showDetailedWelcomeText) {
                        Text(
                            text = stringResource(R.string.app_description),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                horizontal = horizontalPadding, vertical = verticalSpacing / 2
                            )
                        )

                        Text(
                            text = stringResource(R.string.view_concept),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable { showConceptAnimation = true }
                                .padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(verticalSpacing))
                }
                if (isLandscape) {
                    // Landscape: Cards side by side
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(horizontalPadding / 2)
                    ) {
                        DeviceRoleCard(
                            title = stringResource(R.string.advertising_title),
                            description = stringResource(R.string.advertising_description),
                            icon = Icons.Default.Hub,
                            buttonText = stringResource(R.string.start_advertising),
                            onClick = onAdvertising,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            compact = screenHeight < 500.dp
                        )

                        DeviceRoleCard(
                            title = stringResource(R.string.discovering_title),
                            description = stringResource(R.string.discovering_description),
                            icon = Icons.Default.Sensors,
                            buttonText = stringResource(R.string.start_discovery),
                            onClick = onDiscovering,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            compact = screenHeight < 500.dp
                        )
                    }
                } else {
                    // Portrait: Cards stacked
                    DeviceRoleCard(
                        title = stringResource(R.string.advertising_title),
                        description = stringResource(R.string.advertising_description),
                        icon = Icons.Default.Hub,
                        buttonText = stringResource(R.string.start_advertising),
                        onClick = onAdvertising,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        compact = screenHeight < 600.dp
                    )

                    Spacer(modifier = Modifier.height(verticalSpacing))

                    DeviceRoleCard(
                        title = stringResource(R.string.discovering_title),
                        description = stringResource(R.string.discovering_description),
                        icon = Icons.Default.Sensors,
                        buttonText = stringResource(R.string.start_discovery),
                        onClick = onDiscovering,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        compact = screenHeight < 600.dp
                    )
                }
            }

            // Animation overlay
            NetworkConceptAnimation(
                isVisible = showConceptAnimation,
                onDismiss = { showConceptAnimation = false }
            )
        }
    }
}

@Composable
fun DeviceRoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .size(if (compact) 56.dp else 72.dp)
                    .padding(if (compact) 6.dp else 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = if (compact) 4.dp else 8.dp)
            )

            Text(
                text = description,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = if (compact) 4.dp else 8.dp)
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (compact) 8.dp else 16.dp)
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(onSettings: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.app_icon_desc),
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = stringResource(R.string.title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_desc)
                    )
                }
            }
        })
}
