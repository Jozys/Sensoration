package de.schuettslaar.sensoration.domain

import android.util.Log
import de.schuettslaar.sensoration.application.data.PTPMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.Logger

interface PTPHandler {
    fun getAdjustedTime(): Long
}

class MainDevicePTPHandler : PTPHandler {
    override fun getAdjustedTime(): Long {
        return System.currentTimeMillis()
    }
}

class ClientPTPHandler : PTPHandler {
    private var t1: Long = 0
    private var t2: Long = 0
    private var t3: Long = 0
    private var t4: Long = 0

    private var offset: Long = 0


    fun handleMessage(ptpMessage: PTPMessage, clientDevice: ClientDevice) {
        Log.d(javaClass.simpleName, "PTP message: $ptpMessage")
        when (ptpMessage.ptpType) {
            PTPMessage.PTPMessageType.SYNC -> handleSync(ptpMessage)
            PTPMessage.PTPMessageType.FOLLOW_UP -> handleFollowUp(ptpMessage, clientDevice)
            PTPMessage.PTPMessageType.DELAY_RESPONSE -> handleDelayResponse(ptpMessage)
            else -> {
                Logger.getLogger(this.javaClass.simpleName)
                    .warning("PTP message type not supported: " + ptpMessage.ptpType)
            }
        }
    }

    private fun handleDelayResponse(message: PTPMessage) {
        t4 = message.messageTimeStamp

        offset = ((t2 - t1) - (t4 - t3)) / 2
        Logger.getLogger(this.javaClass.simpleName).info(
            "t1: ${formatTimestamp(t1)}, " +
                    "t2: ${formatTimestamp(t2)}, " +
                    "t3: ${formatTimestamp(t3)}, " +
                    "t4: ${formatTimestamp(t4)}"
        )

        Logger.getLogger(this.javaClass.simpleName)
            .info(
                "Calculated new Offset: $offset; " +
                        "Adjusted PTP time: ${formatTimestamp(getAdjustedTime())};" +
                        " unadjusted: ${formatTimestamp(System.currentTimeMillis())}"
            )


    }

    private fun handleFollowUp(message: PTPMessage, clientDevice: ClientDevice) {
//        if (client.connectedDevices.isEmpty()) return // TODO check why this is not updated
        t1 = message.messageTimeStamp

        var delayRequest = PTPMessage(
            messageTimeStamp = t1,
            senderDeviceId = message.senderDeviceId,
            state = message.state,
            ptpType = PTPMessage.PTPMessageType.DELAY_REQUEST,
        )
        clientDevice.sendDelayRequest(delayRequest)
        t3 = System.currentTimeMillis().toLong()
    }

    private fun handleSync(message: PTPMessage) {
        val timestamp = getAdjustedTime()
        Log.d(
            "ClientPTPHandler",
            "Received Sync Message > " +
                    "MasterTime: ${formatTimestamp(message.messageTimeStamp)} " +
                    "adjusted time: ${formatTimestamp(timestamp)} " +
                    "unadjusted time: ${formatTimestamp(System.currentTimeMillis())} " +
                    "offset: ${timestamp - message.messageTimeStamp}" +
                    "offset unadj.: ${System.currentTimeMillis() - message.messageTimeStamp}"
        )

        t1 = 0
        t2 = System.currentTimeMillis().toLong()
        t3 = 0
        t4 = 0
    }

    override fun getAdjustedTime(): Long {
        return System.currentTimeMillis() - offset
    }
}

// Helper function to format timestamp
private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return format.format(date)
}