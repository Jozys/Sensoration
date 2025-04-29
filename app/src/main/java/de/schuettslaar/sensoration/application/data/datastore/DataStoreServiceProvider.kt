package de.schuettslaar.sensoration.application.data.datastore

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object DataStoreServiceProvider {
    private var instance: DataStoreService? = null

    fun initialize(context: Context) {
        if (instance == null) {
            instance = DataStoreService(context)
        }
    }

    fun getInstance(): DataStoreService {
        return instance
            ?: throw IllegalStateException("DataStoreServiceSingleton is not initialized")
    }
}