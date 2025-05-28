package de.schuettslaar.sensoration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import de.schuettslaar.sensoration.presentation.navigation.SensorationNavigationHost
import de.schuettslaar.sensoration.presentation.ui.theme.SensorationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()

        // Keep splash screen visible for a little longer to show the animation
        splashScreen.setKeepOnScreenCondition { true }

        super.onCreate(savedInstanceState)

        // Start a coroutine to remove the splash screen after animation completes
        lifecycleScope.launch {
            delay(1200) // Match animation duration
            splashScreen.setKeepOnScreenCondition { false }
        }

        viewModel.initialize()
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
