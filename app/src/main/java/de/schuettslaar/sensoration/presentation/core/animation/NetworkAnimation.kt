package de.schuettslaar.sensoration.presentation.core.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.schuettslaar.sensoration.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun NetworkConceptAnimation(
    isVisible: Boolean, onDismiss: () -> Unit
) {
    if (isVisible) {
        NetworkConceptDialog(onDismiss = onDismiss)
    }
}

@Composable
private fun NetworkConceptDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = true, dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                IconButton(
                    onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }

                Text(
                    text = stringResource(R.string.network_concept),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp, bottom = 16.dp)
                )

                DeviceNetworkAnimation(
                    modifier = Modifier
                        .padding(top = 60.dp, bottom = 40.dp)
                        .fillMaxSize()
                )

                Text(
                    text = stringResource(R.string.network_concept_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun DeviceNetworkAnimation(modifier: Modifier = Modifier) {
    // Extract theme colors outside of Canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val whiteColor = Color.White

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        // Properly access BoxWithConstraints scope variables
        val centerX = this.constraints.maxWidth / 2f
        val centerY = this.constraints.maxHeight / 2f

        // Make device sizes proportional to the container size
        val containerSize = min(this.constraints.maxWidth, this.constraints.maxHeight).toFloat()
        val mainDeviceRadius = containerSize * 0.10f
        val clientDeviceRadius = containerSize * 0.06f
        val orbitRadius = containerSize * 0.35f

        // Animation states
        val infiniteTransition = rememberInfiniteTransition(label = "networkAnimation")
        val dataPacketProgress = infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ), label = "dataPacketAnimation"
        )

        val chartAnimation = infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(3000), repeatMode = RepeatMode.Reverse
            ), label = "chartAnimation"
        )

        // Number of client devices
        val clientCount = 5

        // Get stroke width in pixels
        val strokeWidth = with(density) { 2.dp.toPx() }

        val devicePainter = rememberVectorPainter(Icons.Default.Smartphone)
        val devicePainter2 = rememberVectorPainter(Icons.Default.TabletAndroid)

        // Draw animation with extracted colors
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw main device
            translate(
                left = centerX - mainDeviceRadius - devicePainter2.intrinsicSize.width / 2f,
                top = centerY - mainDeviceRadius - devicePainter2.intrinsicSize.height / 2f
            ) {
                with(devicePainter2) {
                    draw(
                        size = Size(mainDeviceRadius.dp.toPx(), mainDeviceRadius.dp.toPx()),
                        alpha = 0.5f,
                        colorFilter = ColorFilter.tint(primaryColor),
                    )
                }
            }

//            drawCircle(
//                color = primaryColor,
//                radius = mainDeviceRadius,
//                center = Offset(centerX, centerY)
//            )

            // Draw simplified chart inside main device
            val chartPath = Path().apply {
                val chartWidth = mainDeviceRadius * 1.2f
                val chartHeight = mainDeviceRadius * 0.8f
                val startX = centerX - chartWidth / 2
                val startY = centerY + chartHeight / 2

                moveTo(startX, startY)

                // Create a simple sine-wave like chart
                val steps = 12
                for (i in 0..steps) {
                    val x = startX + (chartWidth * i / steps)
                    val y =
                        startY - chartHeight * sin(i * PI / 3 + chartAnimation.value * 4 * PI).toFloat() * 0.5f
                    lineTo(x, y)
                }
            }

            drawPath(
                path = chartPath, color = whiteColor, style = Stroke(width = strokeWidth)
            )


            // Draw client devices in a circle
            for (i in 0 until clientCount) {
                val angle = 2 * PI * i / clientCount
                val deviceX = centerX + orbitRadius * cos(angle).toFloat()
                val deviceY = centerY + orbitRadius * sin(angle).toFloat()

                // Draw client device
                translate(
                    left = deviceX - clientDeviceRadius - devicePainter.intrinsicSize.width / 2f,
                    top = deviceY - clientDeviceRadius - devicePainter.intrinsicSize.height / 2f
                ) {
                    with(devicePainter) {
                        draw(
                            size = Size(clientDeviceRadius.dp.toPx(), clientDeviceRadius.dp.toPx()),
                            alpha = 0.5f,
                            colorFilter = ColorFilter.tint(secondaryColor),
                        )
                    }
                }

//                drawCircle(
//                    color = secondaryColor,
//                    radius = clientDeviceRadius,
//                    center = Offset(deviceX, deviceY)
//                )

                // Draw data packets being sent to main device
                val packetProgress = (dataPacketProgress.value + i.toFloat() / clientCount) % 1f
                if (packetProgress < 0.9f) { // Only show packets during part of the animation
                    val packetX = deviceX + (centerX - deviceX) * packetProgress
                    val packetY = deviceY + (centerY - deviceY) * packetProgress

                    // Packet size changes as it travels (looks like it's moving in 3D)
                    val packetSize = containerSize * 0.02f * (1f - packetProgress * 0.5f)

                    // Draw packet
                    drawCircle(
                        color = tertiaryColor,
                        radius = packetSize,
                        center = Offset(packetX, packetY)
                    )
                }
            }
        }
    }
}
