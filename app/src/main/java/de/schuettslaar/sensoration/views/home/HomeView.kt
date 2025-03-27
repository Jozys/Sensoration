package de.schuettslaar.sensoration.views.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable()
fun HomeView(onBack: () -> Unit) {
    Text(
        text = "Hallo Welt!",
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(16.dp)
    )
}