package de.schuettslaar.sensoration.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.graphics.Color

@SuppressLint("DiscouragedApi")
fun getStringResourceByName(context: Context, resName: String): String {
    try {
        var text = context.getString(
            context.resources.getIdentifier(resName, "string", context.packageName)
        )
        return text
    } catch (e: Exception) {
        return resName;
    }
    return resName
}

fun generateColorBasedOnName(name: String): Color {
    // Use the hash code of the string as a seed
    val hash = name.hashCode()

    // Generate HSV values from the hash
    // Hue: Use modulo 360 to get a value between 0-359 degrees
    val hue = (hash and 0xFF).toFloat() / 255f * 360f

    // Keep saturation and value high for vibrant, distinguishable colors
    val saturation = 0.7f + (((hash shr 8) and 0xFF) / 255f) * 0.3f
    val value = 0.8f + (((hash shr 16) and 0xFF) / 255f) * 0.2f

    // Convert HSV to RGB, then to Color
    return Color.hsv(hue, saturation, value)
}