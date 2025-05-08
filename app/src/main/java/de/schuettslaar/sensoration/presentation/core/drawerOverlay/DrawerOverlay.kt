package de.schuettslaar.sensoration.presentation.core.drawerOverlay

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


/**
 * A custom composable that provides a side drawer overlay.
 * This drawer slides in and out from either the left or right side of the screen and can be used to
 * display additional options or information.
 *
 * @param isDrawerOpen Boolean indicating whether the drawer is open.
 * @param onDismiss Callback function that gets called when the drawer should be dismissed.
 * @param drawerContent Composable content that is displayed inside the drawer.
 * @param modifier Modifier to be applied to the drawer overlay container.
 * @param drawerWidth Width of the drawer in Dp.
 * @param animationDuration Duration of the drawer open/close animation in milliseconds.
 * @param maskColor Color of the mask overlay when the drawer is open.
 * @param showMask Boolean indicating whether to show the mask overlay.
 * @param drawerSide Side of the screen where the drawer appears (left or right).
 * @param cornerRadius Corner radius of the drawer for rounded edges.
 * @param dragThresholdFraction Fraction of the drawer's width that must be dragged to open/close it.
 * @param enableSwipe Boolean indicating whether swipe gestures are enabled to open/close the drawer.
 * @param offsetMultiplier Multiplier for the offset animation of the drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerOverlay(
    isDrawerOpen: Boolean,
    onDismiss: () -> Unit,
    title: @Composable () -> Unit,
    drawerContent: @Composable ColumnScope.() -> Unit,
    mainContentModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 300.dp,
    animationDuration: Int = 300,
    maskColor: Color = Color.Black.copy(alpha = 0.5f),
    showMask: Boolean = false,
    drawerSide: DrawerSide = DrawerSide.RIGHT,
    cornerRadius: Dp = 32.dp,
    dragThresholdFraction: Float = 0.5f,
    enableSwipe: Boolean = true,
    offsetMultiplier: Int = 2
) {

    // Coroutine scope for managing animations
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    // Width of the drawer in pixels
    val drawerWidthPx = with(density) { drawerWidth.toPx() }

    // Offset for the drawer animation
    val offsetX =
        remember { Animatable(if (isDrawerOpen) 0f else drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)) }

    // Launch animation when the drawer state changes
    LaunchedEffect(isDrawerOpen) {
        val targetOffsetX =
            if (isDrawerOpen) 0f else drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)
        offsetX.animateTo(
            targetValue = targetOffsetX,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    if (isDrawerOpen) {
        BackHandler {
            onDismiss()
        }
    }

    Scaffold(topBar = {
        DrawerTopBar(
            title = title,
            onClose = {
                onDismiss()
            },
            modifier = Modifier.offset {
                IntOffset(
                    x = offsetMultiplier * offsetX.value.roundToInt(),
                    y = 0
                )
            })
    }, modifier = mainContentModifier.pointerInput(Unit) {
        if (enableSwipe) {
            detectDragGestures(
                onDragEnd = {
                    scope.launch {
                        val shouldClose = when (drawerSide) {
                            DrawerSide.LEFT -> offsetX.value < -drawerWidthPx * dragThresholdFraction
                            DrawerSide.RIGHT -> offsetX.value > drawerWidthPx * dragThresholdFraction
                            DrawerSide.NONE -> true
                        }

                        val finalTarget = if (shouldClose) {
                            drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)
                        } else {
                            0f
                        }

                        offsetX.animateTo(
                            targetValue = finalTarget,
                            animationSpec = tween(durationMillis = animationDuration)
                        )

                        if (shouldClose) {
                            onDismiss()
                        }
                    }
                }
            ) { change, dragAmount ->
                change.consume()

                scope.launch {
                    val newOffset = offsetX.value + dragAmount.x

                    val clampedOffset = when (drawerSide) {
                        DrawerSide.LEFT -> newOffset.coerceIn(-drawerWidthPx, 0f)
                        DrawerSide.RIGHT -> newOffset.coerceIn(0f, drawerWidthPx)
                        DrawerSide.NONE -> 0f
                    }

                    offsetX.snapTo(clampedOffset)
                }
            }
        }
    }) { innerPadding ->
        // Main screen content

        // Mask overlay when the drawer is open
        if (isDrawerOpen && showMask) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(maskColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDismiss() })
                    }
            )
        }

        // Drawer content
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .offset { IntOffset(x = offsetMultiplier * offsetX.value.roundToInt(), y = 0) }
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = if (cornerRadius > 0.dp) {
                        if (drawerSide == DrawerSide.LEFT) {
                            RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)
                        } else {
                            RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius)
                        }
                    } else {
                        RectangleShape
                    }
                )


        ) {
            // Content inside the drawer
            Column {
                drawerContent()
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerTopBar(
    title: @Composable () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                modifier = modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = {
                    onClose()
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = null
                    )
                }
                title()
            }
        },
    )
}