package org.cryptotrader.security.library.config

import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.entity.ip.UserIpAddress
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.security.library.service.entity.UserIpAddressEntityService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.support.GenericMessage

@DisplayName("User IP Detection Consumer Config")
class UserIpDetectionConsumerConfigTest : CryptoTraderTest() {
    private lateinit var userIpAddressEntityService: UserIpAddressEntityService
    private lateinit var consumerConfig: UserIpDetectionConsumerConfig

    @BeforeEach
    fun setUp() {
        this.userIpAddressEntityService = mock(UserIpAddressEntityService::class.java)
        this.consumerConfig = UserIpDetectionConsumerConfig(this.userIpAddressEntityService)
    }

    @Nested
    @DisplayName("Consumer Function")
    inner class ConsumerFunction {

        @Test
        @DisplayName("Should record user IP when event payload is valid")
        fun userIpDetection_RecordsUserIp_WhenPayloadIsValid() {
            val user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 42L }
            val event = UserIpDetectionEvent("192.168.1.50", user)
            val message = GenericMessage(event)

            val expectedIp = UserIpAddress(user, "192.168.1.50")
            `when`(userIpAddressEntityService.recordUserIp(user, "192.168.1.50")).thenReturn(expectedIp)

            val consumer = consumerConfig.userIpDetection()
            consumer.accept(message)

            verify(userIpAddressEntityService, times(1)).recordUserIp(user, "192.168.1.50")
        }

        @Test
        @DisplayName("Should not record user IP when ip is blank")
        fun userIpDetection_DoesNotRecord_WhenIpIsBlank() {
            val user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 42L }
            val event = UserIpDetectionEvent("", user)
            val message = GenericMessage(event)

            val consumer = consumerConfig.userIpDetection()
            consumer.accept(message)

            verifyNoInteractions(userIpAddressEntityService)
        }

        @Test
        @DisplayName("Should handle service exception gracefully without throwing")
        fun userIpDetection_HandlesExceptionGracefully() {
            val user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 42L }
            val event = UserIpDetectionEvent("192.168.1.50", user)
            val message = GenericMessage(event)

            `when`(userIpAddressEntityService.recordUserIp(user, "192.168.1.50")).thenThrow(RuntimeException("DB error"))

            val consumer = consumerConfig.userIpDetection()
            consumer.accept(message)

            verify(userIpAddressEntityService, times(1)).recordUserIp(user, "192.168.1.50")
        }
    }

    @Nested
    @DisplayName("Conditional Configuration")
    inner class ConditionalConfiguration {
        private val contextRunner = ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfig::class.java, UserIpDetectionConsumerConfig::class.java))

        @Test
        @DisplayName("Should not create consumer bean when UserIpAddressEntityService is missing")
        fun consumerBean_NotCreated_WhenServiceIsMissing() {
            contextRunner.run { context ->
                assertThat(context).doesNotHaveBean(UserIpDetectionConsumerConfig::class.java)
                assertThat(context).doesNotHaveBean("userIpDetection")
                assertThat(context).doesNotHaveBean("userIpDetectionConsumer")
            }
        }

        @Test
        @DisplayName("Should create consumer bean when UserIpAddressEntityService is present")
        fun consumerBean_Created_WhenServiceIsPresent() {
            contextRunner
                .withUserConfiguration(MockServiceConfig::class.java)
                .run { context ->
                    assertThat(context).hasSingleBean(UserIpDetectionConsumerConfig::class.java)
                    assertThat(context).hasBean("userIpDetection")
                }
        }
    }

    @Configuration(proxyBeanMethods = false)
    open class MockServiceConfig {
        @Bean
        open fun userIpAddressEntityService(): UserIpAddressEntityService {
            return mock(UserIpAddressEntityService::class.java)
        }
    }
}
