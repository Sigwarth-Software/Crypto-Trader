package org.cryptotrader.api.config

import org.cryptotrader.console.library.communication.request.ConsoleCommandRequest
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.component.ConsoleRequestGateway
import org.cryptotrader.universal.library.events.config.BaseGatewayConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration
open class ConsoleResponsesConfig(
    gateway: ConsoleRequestGateway
) : BaseGatewayConfig<ConsoleCommandRequest, ConsoleCommandResponse>(gateway) {
    override val log: Logger = LoggerFactory.getLogger(ConsoleResponsesConfig::class.java)

    @Bean(name = ["consoleResponsesConsumer"])
    open fun consoleResponsesConsumer(): Consumer<Message<ConsoleCommandResponse>> {
        return this.createConsumer()
    }
}
