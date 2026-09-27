package org.cryptotrader.logging.config

import org.cryptotrader.logging.library.events.ApplicationLogEventPayload
import org.cryptotrader.logging.library.service.ApplicationLogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.time.LocalDateTime
import java.util.function.Consumer

@Configuration
open class ApplicationLogConsumerConfig(
    private val persistenceService: ApplicationLogService
) {
    private val log = LoggerFactory.getLogger(ApplicationLogConsumerConfig::class.java)

    @Bean(name = ["applicationLogsConsumer"])
    open fun applicationLogsConsumer(): Consumer<Message<ApplicationLogEventPayload>> {
        return Consumer { message ->
            val entry: ApplicationLogEventPayload = message.payload
            log.debug("Received application log entry for module {}", entry.module)
            this.persistenceService.persist(entry, LocalDateTime.now())
        }
    }
}
