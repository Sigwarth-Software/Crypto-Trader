package org.cryptotrader.console.library.events

import org.cryptotrader.console.library.communication.request.ConsoleCommandRequest
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.component.ConsoleAuthenticationRunner
import org.cryptotrader.console.library.component.ConsoleEventBinding
import org.cryptotrader.console.library.services.ConsoleCommandService
import org.cryptotrader.universal.library.events.BaseEventConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration
open class ConsoleEventsListener @Autowired constructor(
    streamBridge: StreamBridge,
    private val consoleCommandService: ConsoleCommandService,
    private val consoleAuthenticationRunner: ConsoleAuthenticationRunner
) : BaseEventConsumer<ConsoleCommandRequest, ConsoleCommandResponse>(
    streamBridge,
    ConsoleEventBinding.CONSOLE_RESPONSES.bindingName
) {
    override val log: Logger = LoggerFactory.getLogger(ConsoleEventsListener::class.java)

    @Bean(name = ["consoleRequestsConsumer"])
    open fun consoleRequestsConsumer(): Consumer<Message<ConsoleCommandRequest>> {
        return this.createConsumer { request, authorizationHeader ->
            log.info("Received console command: {}", request)
            this.consoleAuthenticationRunner.runWithAuthorizationHeader(authorizationHeader) {
                this.consoleCommandService.executeCommand(request)
            }
        }
    }

    override fun handleError(exception: Exception): ConsoleCommandResponse {
        return ConsoleCommandResponse(
            "An error occurred: ${exception.message}",
            null
        )
    }
}