package de.schuettslaar.sensoration.domain

import de.schuettslaar.sensoration.adapter.nearby.NearbyWrapper

class Device {
    private val wrapper: NearbyWrapper? = null
    private val isMaster = false
    private val applicationStatus : ApplicationStatus = ApplicationStatus.INIT;
}
