package org.cryptotrader.logging.config

import org.cryptotrader.logging.library.events.ExecutionSpeedLogEventPayload
import org.cryptotrader.logging.library.entity.ExecutionSpeedWarningLevel
import org.cryptotrader.logging.library.service.ExecutionSpeedLogService
import org.cryptotrader.logging.properties.TimeTrackingProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration
open class ExecutionSpeedLogConsumerConfig(
    private val persistenceService: ExecutionSpeedLogService,
    private val timeTrackingProperties: TimeTrackingProperties,
) {
    private val log = LoggerFactory.getLogger(ExecutionSpeedLogConsumerConfig::class.java)

    @Bean(name = ["executionSpeedLogsConsumer"])
    open fun executionSpeedLogsConsumer(): Consumer<Message<ExecutionSpeedLogEventPayload>> {
        return Consumer { message ->
            val entry: ExecutionSpeedLogEventPayload = message.payload
            log.debug("Received execution speed log for {}()", entry.fullMethodQualifiedName)
            val warningLevel = ExecutionSpeedWarningLevel.from(
                entry.executionSpeed,
                entry.expectedExecutionSpeed,
                timeTrackingProperties.exceedingFactor,
                timeTrackingProperties.expectedFactor,
                timeTrackingProperties.warningFactor,
                timeTrackingProperties.alertFactor,
            )
            persistenceService.persist(entry, warningLevel)
        }
    }
}
