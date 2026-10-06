package org.cryptotrader.security.library.event.publisher

import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.event.SecurityEventBinding
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.universal.library.events.EventPublisher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*

@DisplayName("Security Events Publisher")
class SecurityEventsPublisherTest : CryptoTraderTest() {
    private lateinit var eventPublisher: EventPublisher
    private lateinit var securityEventsPublisher: SecurityEventsPublisher

    @BeforeEach
    fun setUp() {
        this.eventPublisher = mock(EventPublisher::class.java)
        this.securityEventsPublisher = SecurityEventsPublisher(this.eventPublisher)
    }

    @Nested
    @DisplayName("Publish")
    inner class Publish {

        @Test
        @DisplayName("Should publish UserIpDetectionEvent to USER_IP_DETECTION_REQUESTS binding")
        fun publish_PublishesUserIpDetectionEvent() {
            val user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 1L }
            val event = UserIpDetectionEvent("192.168.1.1", user)

            securityEventsPublisher.publish(event)

            verify(eventPublisher, times(1)).publish(
                SecurityEventBinding.USER_IP_DETECTION_REQUESTS.bindingName,
                event
            )
        }

        @Test
        @DisplayName("Should skip publish when underlying eventPublisher is null")
        fun publish_SkipsWhenNull() {
            val publisherWithNull = SecurityEventsPublisher(null)
            val user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 1L }
            val event = UserIpDetectionEvent("192.168.1.1", user)

            publisherWithNull.publish(event)
            // No exception thrown
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for unsupported event types")
        fun publish_ThrowsOnUnsupportedEvent() {
            assertThrows<IllegalArgumentException> {
                securityEventsPublisher.publish("Unsupported event string")
            }
        }
    }
}
