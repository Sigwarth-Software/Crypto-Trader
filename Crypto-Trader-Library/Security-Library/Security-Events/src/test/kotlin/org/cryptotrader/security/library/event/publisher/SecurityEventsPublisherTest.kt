package org.cryptotrader.security.library.event.publisher

import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.event.SecurityEventBinding
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.cryptotrader.universal.library.events.EventPublisher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

@DisplayName("SecurityEventsPublisher")
@UnitSuite
class SecurityEventsPublisherTest : CryptoTraderTest() {
    private lateinit var eventPublisher: EventPublisher
    private lateinit var securityEventsPublisher: SecurityEventsPublisher

    @BeforeEach
    fun setUp() {
        this.eventPublisher = mock(EventPublisher::class.java)
        this.securityEventsPublisher = SecurityEventsPublisher(this.eventPublisher)
    }

    @Nested
    @DisplayName("publish")
    inner class Publish {

        @Test
        @DisplayName("should publish to the user IP detection requests binding")
        fun publishToUserIpDetectionBinding() {
            val user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 1L }
            val event = UserIpDetectionEvent("192.168.1.1", user)

            securityEventsPublisher.publish(event)

            verify(eventPublisher, times(1)).publish(
                SecurityEventBinding.USER_IP_DETECTION_REQUESTS.bindingName,
                event
            )
        }

        @Nested
        @DisplayName("when underlying eventPublisher is null")
        inner class WhenUnderlyingEventPublisherIsNull {
            private lateinit var publisherWithNull: SecurityEventsPublisher
            private lateinit var event: UserIpDetectionEvent

            @BeforeEach
            fun setUp() {
                publisherWithNull = SecurityEventsPublisher(null)
                val user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 1L }
                event = UserIpDetectionEvent("192.168.1.1", user)
            }

            @Test
            @DisplayName("should skip publish")
            fun skipPublish() {
                publisherWithNull.publish(event)
                // No exception thrown
            }
        }

        @Nested
        @DisplayName("when the event type is unsupported")
        inner class WhenTheEventTypeIsUnsupported {
            @Test
            @DisplayName("should throw IllegalArgumentException for unsupported event types")
            fun throwIllegalArgumentExceptionForUnsupportedEventTypes() {
                assertThrows<IllegalArgumentException> {
                    securityEventsPublisher.publish("Unsupported event string")
                }
            }
        }
    }
}
