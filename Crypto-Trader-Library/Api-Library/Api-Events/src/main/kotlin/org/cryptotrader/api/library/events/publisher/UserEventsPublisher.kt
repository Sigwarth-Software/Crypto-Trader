package org.cryptotrader.api.library.events.publisher

import org.cryptotrader.api.library.events.ApiEventBinding
import org.cryptotrader.universal.library.events.EventPublisher
import org.cryptotrader.api.library.events.UserRegisteredEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserEventsPublisher @Autowired constructor(
    @Autowired(required = false)
    private val eventPublisher: EventPublisher?
) {
    private val log = LoggerFactory.getLogger(UserEventsPublisher::class.java)

    fun publishUserRegisteredEvent(registerEvent: UserRegisteredEvent) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publish(ApiEventBinding.USER_REGISTERED.bindingName, registerEvent)
        } else {
            log.debug("EventPublisher unavailable; skipping publishUserRegisteredEvent in docs/non-stream context.")
        }
    }
}