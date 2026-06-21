package org.cryptotrader.security.library.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.cryptotrader.security.library.service.EncryptionService
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.converter.AbstractMessageConverter
import org.springframework.util.MimeType
import org.slf4j.LoggerFactory
import java.util.Base64

class EventMessageEncryptionConverter(
    private val objectMapper: ObjectMapper,
    private val encryptionService: EncryptionService
) : AbstractMessageConverter(ENCRYPTED_JSON_MIME_TYPE) {
    private val log = LoggerFactory.getLogger(javaClass)

    init {
        this.setSerializedPayloadClass(ByteArray::class.java)
    }

    override fun supports(clazz: Class<*>): Boolean = true

    override fun convertToInternal(payload: Any, headers: MessageHeaders?, conversionHint: Any?): Any {
        val plaintext = this.objectMapper.writeValueAsBytes(payload)
        val ciphertext = this.encryptionService.encrypt(plaintext, this.associatedData())
        val encodedCiphertext = Base64.getEncoder().encodeToString(ciphertext)
        return encodedCiphertext.toByteArray(Charsets.UTF_8)
    }

    override fun convertFromInternal(message: Message<*>, targetClass: Class<*>, conversionHint: Any?): Any {
        val encodedCiphertext = when (val payload = message.payload) {
            is ByteArray -> String(payload, Charsets.UTF_8)
            is String -> payload
            else -> throw IllegalArgumentException(
                "Encrypted Kafka payload must be ByteArray or String, but was ${payload.javaClass.name}"
            )
        }
        val ciphertext = Base64.getDecoder().decode(encodedCiphertext)
        val plaintext = this.encryptionService.decrypt(ciphertext, this.associatedData())
        return this.objectMapper.readValue(plaintext, targetClass)
    }

    private fun associatedData(): ByteArray {
        return DEFAULT_ASSOCIATED_DATA.toByteArray(Charsets.UTF_8)
    }

    companion object {
        const val ENCRYPTED_JSON_CONTENT_TYPE: String = "application/vnd.cryptotrader.encrypted+json"
        const val EVENT_BINDING_HEADER: String = "ct-event-binding"
        private const val DEFAULT_ASSOCIATED_DATA: String = "cryptotrader.kafka.event.v1"
        val ENCRYPTED_JSON_MIME_TYPE: MimeType = MimeType.valueOf(ENCRYPTED_JSON_CONTENT_TYPE)
    }
}
