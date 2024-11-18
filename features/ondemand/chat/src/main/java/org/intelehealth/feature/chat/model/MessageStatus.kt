package org.intelehealth.feature.chat.model


/**
 * Created by Vaghela Mithun R. on 16-03-2023 - 20:52.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 */
enum class MessageStatus(val value: Int) {

    RECEIVED(4), CMD_RECEIVED(5), READ(3), DELIVERED(2), SENT(1), SENDING(0), FAIL(-1);

    fun isReceived() = this == RECEIVED

    fun isDelivered() = this == DELIVERED

    fun isRead() = this == READ

    fun isCmdReceived() = this == CMD_RECEIVED

    fun isSent() = this == SENT

    fun isSending() = this == SENDING

    fun isFail() = this == FAIL

    companion object {
        @JvmStatic
        fun getStatus(value: Int) = when (value) {
            READ.value -> READ
            SENDING.value -> SENDING
            SENT.value -> SENT
            DELIVERED.value -> DELIVERED
            else -> SENT
        }
    }
}