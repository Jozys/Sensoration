package de.schuettslaar.sensoration.domain.exception

class MissingPermissionException(private var permission: String) : Exception() {
    override val message: String
        get() = "Missing permission to access the sensor data. Please check your app permissions."

    fun getMissingPermission(): String {
        return permission
    }
}