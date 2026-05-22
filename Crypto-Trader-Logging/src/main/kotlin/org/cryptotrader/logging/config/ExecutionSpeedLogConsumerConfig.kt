package org.cryptotrader.logging.config

import org.cryptotrader.logging.library.events.ExecutionSpeedLogEventPayload
import org.cryptotrader.logging.library.service.ExecutionSpeedLogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration
open class ExecutionSpeedLogConsumerConfig(
    private val persistenceService: ExecutionSpeedLogService
) {
    private val log = LoggerFactory.getLogger(ExecutionSpeedLogConsumerConfig::class.java)

    @Bean(name = ["executionSpeedLogsConsumer"])
    open fun executionSpeedLogsConsumer(): Consumer<Message<ExecutionSpeedLogEventPayload>> {
        return Consumer { message ->
            val entry = message.payload
            log.debug("Received execution speed log for {}", entry.fullMethodQualifiedName)
            persistenceService.persist(entry)
        }
    }
}
