package org.cryptotrader.logging.config

import org.cryptotrader.logging.library.events.ApplicationExceptionEventPayload
import org.cryptotrader.logging.library.service.ApplicationExceptionService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.time.LocalDateTime
import java.util.function.Consumer

@Configuration
open class ApplicationExceptionConsumerConfig(
    private val persistenceService: ApplicationExceptionService
) {
    private val log = LoggerFactory.getLogger(ApplicationExceptionConsumerConfig::class.java)

    @Bean(name = ["applicationExceptionsConsumer"])
    open fun applicationExceptionsConsumer(): Consumer<Message<ApplicationExceptionEventPayload>> {
        return Consumer { message ->
            val entry: ApplicationExceptionEventPayload = message.payload
            log.debug("Received application exception for module {}: {}", entry.module, entry.exceptionClass)
            this.persistenceService.persist(entry, LocalDateTime.now())
        }
    }
}
