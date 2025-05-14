package de.schuettslaar.sensoration.domain

import de.schuettslaar.sensoration.application.data.PTPMessage
import java.util.Date
import java.util.logging.Logger

interface PTPHandler {
    fun getAdjustedTime(): Long
}

class ClientPTPHandler : PTPHandler {
    private var t1: Long = 0
    private var t2: Long = 0
    private var t3: Long = 0
    private var t4: Long = 0

    private var offset: Long = 0


    fun handleMessage(ptpMessage: PTPMessage, client: Client) {
        when (ptpMessage.ptpType) {
            PTPMessage.PTPMessageType.SYNC -> handleSync(ptpMessage)
            PTPMessage.PTPMessageType.FOLLOW_UP -> handleFollowUp(ptpMessage, client)
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
        Logger.getLogger(this.javaClass.simpleName)
            .info("Calculated new Offset: $offset")
        Logger.getLogger(this.javaClass.simpleName)
            .info("t1: $t1, t2: $t2, t3: $t3, t4: $t4")

        Date(System.currentTimeMillis() + offset).let {
            Logger.getLogger(this.javaClass.simpleName)
                .info("Adjusted time: $it; unadjusted: ${Date(System.currentTimeMillis())}")
        }

    }

    private fun handleFollowUp(message: PTPMessage, client: Client) {
        t1 = message.messageTimeStamp

        var delayRequest = PTPMessage(
            messageTimeStamp = t1,
            senderDeviceId = message.senderDeviceId,
            state = message.state,
            ptpType = PTPMessage.PTPMessageType.DELAY_REQUEST,
        )
        client.sendDelayRequest(delayRequest)
        t3 = System.currentTimeMillis().toLong()
    }

    private fun handleSync(message: PTPMessage) {
        t1 = 0
        t2 = System.currentTimeMillis().toLong()
        t3 = 0
        t4 = 0
    }

    override fun getAdjustedTime(): Long {
        return System.currentTimeMillis() + offset
    }
}