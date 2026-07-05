package org.cryptotrader.universal.library.events

import org.slf4j.Logger
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import java.util.function.Consumer

abstract class BaseEventConsumer<Request, Response>(
    private val streamBridge: StreamBridge,
    private val outboundBindingName: String
) {
    protected abstract val log: Logger
    private val eventBindingHeader: String = "ct-event-binding"

    protected fun createConsumer(processor: (Request, String?) -> Response): Consumer<Message<Request>> {
        return Consumer { message ->
            val request: Request = message.payload
            val correlationIdHeader = message.headers["correlationId"]
            val rawAuth: Any? = message.headers["Authorization"]
            val authorizationHeader: String? = when (rawAuth) {
                is String -> rawAuth
                is ByteArray -> try {
                    String(rawAuth, Charsets.UTF_8)
                } catch (exception: Exception) { null }
                else -> null
            }

            log.debug("Processing request with correlationId={}", correlationIdHeader)

            val result: Response = try {
                processor(request, authorizationHeader)
            } catch (exception: Exception) {
                log.error("Error processing request correlationId={}", correlationIdHeader, exception)
                handleError(exception)
            }

            val responseMessageBuilder = MessageBuilder
                .withPayload(result)
                .setHeader("correlationId", correlationIdHeader)
                .setHeader(this.eventBindingHeader, this.outboundBindingName)

            if (authorizationHeader != null) {
                responseMessageBuilder.setHeader("Authorization", authorizationHeader)
            }

            this.streamBridge.send(this.outboundBindingName, responseMessageBuilder.build())
        }
    }

    protected abstract fun handleError(exception: Exception): Response
}
