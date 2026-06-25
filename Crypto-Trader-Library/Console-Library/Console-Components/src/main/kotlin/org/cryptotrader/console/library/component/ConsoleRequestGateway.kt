package org.cryptotrader.console.library.component

import org.cryptotrader.console.library.communication.request.ConsoleCommandRequest
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.universal.library.events.EventPublisher
import org.cryptotrader.universal.library.events.RequestGatewayController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ConsoleRequestGateway @Autowired constructor(
    override val eventPublisher: EventPublisher
) : RequestGatewayController<ConsoleCommandRequest, ConsoleCommandResponse>() {
    private val log: Logger = LoggerFactory.getLogger(ConsoleRequestGateway::class.java)
}