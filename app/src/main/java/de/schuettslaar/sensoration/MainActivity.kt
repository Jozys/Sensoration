package de.schuettslaar.sensoration

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import de.schuettslaar.sensoration.navigation.SensorationNavigationHost
import de.schuettslaar.sensoration.ui.theme.SensorationTheme

class MainActivity : ComponentActivity() {
    //private var wrapper: NearbyWrapper2 = NearbyWrapper2(this);
    private val viewModel by viewModels<MainViewModel>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SensorationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NearbyComp(
                        "Hallo Welt",
                        Modifier.padding(innerPadding),
                        { requestPermissions() },
                        viewModel.text,
                        viewModel.possibleConnections,
                        {
                            viewModel.startDiscovering()
                        },
                        {
                            viewModel.startAdvertising()
                        }
                    )
                }
            }
        }
    }

    fun requestPermissions() {
        var requiredPermissions: Array<String>

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.NEARBY_WIFI_DEVICES,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requiredPermissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            requiredPermissions = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }

        if (!hasPermissions(this, permissions = requiredPermissions)) {
            if (Build.VERSION.SDK_INT < 23) {
                ActivityCompat.requestPermissions(
                    this, requiredPermissions, 1
                )
            } else {
                requestPermissions(requiredPermissions, 1)
            }
        }
    }

    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}

@Composable
fun NearbyComp(
    name: String,
    modifier: Modifier = Modifier,
    requestPermissions: () -> Unit,
    text: String,
    possibleDevices: MutableMap<String, DiscoveredEndpointInfo>,
    onStartDiscovery: () -> Unit,
    onStartAdvertising: () -> Unit
) {


    Column(modifier = modifier) {
        Button(onClick = {
            requestPermissions()
            onStartDiscovery()
        }) {
            Text("Start Discovery")
        }
        Button(modifier = modifier, onClick = {
            requestPermissions()
            onStartAdvertising()
        }) {
            Text("Start Advertising")
        }

        Text(text)

        Text(possibleDevices.keys.size.toString())

    }
fun AppContent(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    SensorationNavigationHost(navController, modifier = modifier)
}
