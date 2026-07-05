package org.cryptotrader.logging.library.events.publisher

import org.cryptotrader.logging.library.events.ExecutionSpeedLogEventPayload
import org.cryptotrader.logging.library.events.LogBatchEvent
import org.cryptotrader.logging.library.events.LogEventBinding
import org.cryptotrader.universal.library.events.EventPublisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LogEventsPublisher @Autowired constructor(
    @Autowired(required = false)
    private val eventPublisher: EventPublisher?
) {
    private val log: Logger = LoggerFactory.getLogger(LogEventsPublisher::class.java)


    fun <T> publish(event: T) {
        if (this.eventPublisher == null) {
            log.debug("EventPublisher unavailable; skipping publish in docs/non-stream context.")
            return
        }
        when (event) {
            is LogBatchEvent -> this.publishBatch(event)
            is ExecutionSpeedLogEventPayload -> this.publishExecutionSpeed(event)
            else -> throw IllegalArgumentException("Unsupported event type: ${event?.javaClass?.name ?: "unknown"}")
        }
    }

    fun publishBatch(event: LogBatchEvent) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publish(LogEventBinding.FRONTEND_LOGS_REQUESTS.bindingName, event)
        } else {
            log.debug("EventPublisher unavailable; skipping publishBatch in docs/non-stream context.")
        }
    }

    fun publishExecutionSpeed(event: ExecutionSpeedLogEventPayload) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publish(LogEventBinding.EXECUTION_SPEED_LOGS_REQUESTS.bindingName, event)
        } else {
            log.warn("EventPublisher unavailable; skipping publishExecutionSpeed in docs/non-stream context.")
        }
    }
}
