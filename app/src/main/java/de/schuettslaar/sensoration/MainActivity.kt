package de.schuettslaar.sensoration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import de.schuettslaar.sensoration.navigation.SensorationNavigationHost
import de.schuettslaar.sensoration.ui.theme.SensorationTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.requestPermissions {
            it
            requestPermissions(it, 0)
        }

        enableEdgeToEdge()
        setContent {
            SensorationTheme {
                AppContent()
            }
        }
    }


}

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    SensorationNavigationHost(navController, modifier = modifier)
}
