package de.schuettslaar.sensoration.utils

import android.annotation.SuppressLint
import android.content.Context

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