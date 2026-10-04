package org.cryptotrader.security.library.event.publisher

import org.cryptotrader.security.library.event.SecurityEventBinding
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.universal.library.events.EventPublisher
import org.cryptotrader.universal.library.events.model.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SecurityEventsPublisher @Autowired constructor(
    @Autowired(required = false)
    private val eventPublisher: EventPublisher?
) : Publisher {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SecurityEventsPublisher::class.java)
    }

    override fun <T> publish(event: T) {
        if (this.eventPublisher == null) {
            log.debug("EventPublisher unavailable. Skipping publish.")
            return
        }

        when (event) {
            is UserIpDetectionEvent -> {
                this.eventPublisher.publish(SecurityEventBinding.USER_IP_DETECTION_REQUESTS.bindingName, event)
            }
            else -> throw IllegalArgumentException("Unsupported event type: ${event?.javaClass?.name ?: "unknown"}")
        }
    }
}