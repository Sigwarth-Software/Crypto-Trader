package org.cryptotrader.universal.library.events.config

import org.cryptotrader.universal.library.events.RequestGatewayController
import org.slf4j.Logger
import org.springframework.messaging.Message
import java.util.function.Consumer

abstract class BaseGatewayConfig<Request, Response>(
    private val gateway: RequestGatewayController<Request, Response>
) {
    protected abstract val log: Logger

    protected fun createConsumer(): Consumer<Message<Response>> {
        return Consumer { message ->
            val correlationId = message.headers["correlationId"]
            log.debug("Dispatching response to gateway handler: correlationId={}", correlationId)
            this.gateway.handleResponse(message)
        }
    }
}
