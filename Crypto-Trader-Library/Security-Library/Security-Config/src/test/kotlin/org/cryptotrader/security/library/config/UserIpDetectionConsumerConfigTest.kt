package org.cryptotrader.security.library.config

import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.entity.ip.UserIpAddress
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.security.library.service.entity.UserIpAddressEntityService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.IntegrationSuite
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

@DisplayName("UserIpDetectionConsumerConfig")
@IntegrationSuite
class UserIpDetectionConsumerConfigTest : CryptoTraderTest() {
    private lateinit var userIpAddressEntityService: UserIpAddressEntityService
    private lateinit var consumerConfig: UserIpDetectionConsumerConfig

    @BeforeEach
    fun setUp() {
        this.userIpAddressEntityService = mock(UserIpAddressEntityService::class.java)
        this.consumerConfig = UserIpDetectionConsumerConfig(this.userIpAddressEntityService)
    }

    @Nested
    @DisplayName("userIpDetection")
    inner class ConsumerFunction {

        @Nested
        @DisplayName("when event payload is valid")
        inner class WhenEventPayloadIsValid {
            private lateinit var user: ProductUser
            private lateinit var message: GenericMessage<UserIpDetectionEvent>

            @BeforeEach
            fun setUp() {
                user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 42L }
                val event = UserIpDetectionEvent("192.168.1.50", user)
                message = GenericMessage(event)
                val expectedIp = UserIpAddress(user, "192.168.1.50")
                `when`(userIpAddressEntityService.recordUserIp(user, "192.168.1.50")).thenReturn(expectedIp)
            }

            @Test
            @DisplayName("should record user IP")
            fun recordUserIP() {
                val consumer = consumerConfig.userIpDetection()
                consumer.accept(message)

                verify(userIpAddressEntityService, times(1)).recordUserIp(user, "192.168.1.50")
            }
        }

        @Nested
        @DisplayName("when ip is blank")
        inner class WhenIpIsBlank {
            private lateinit var message: GenericMessage<UserIpDetectionEvent>

            @BeforeEach
            fun setUp() {
                val user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 42L }
                val event = UserIpDetectionEvent("", user)
                message = GenericMessage(event)
            }

            @Test
            @DisplayName("should not record user IP")
            fun notRecordUserIP() {
                val consumer = consumerConfig.userIpDetection()
                consumer.accept(message)

                verifyNoInteractions(userIpAddressEntityService)
            }
        }

        @Nested
        @DisplayName("when the service throws an exception")
        inner class WhenTheServiceThrowsAnException {
            private lateinit var user: ProductUser
            private lateinit var message: GenericMessage<UserIpDetectionEvent>

            @BeforeEach
            fun setUp() {
                user = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 42L }
                val event = UserIpDetectionEvent("192.168.1.50", user)
                message = GenericMessage(event)
                `when`(userIpAddressEntityService.recordUserIp(user, "192.168.1.50")).thenThrow(RuntimeException("DB error"))
            }

            @Test
            @DisplayName("should handle service exception gracefully without throwing")
            fun handleServiceExceptionGracefullyWithoutThrowing() {
                val consumer = consumerConfig.userIpDetection()
                consumer.accept(message)

                verify(userIpAddressEntityService, times(1)).recordUserIp(user, "192.168.1.50")
            }
        }
    }

    @Nested
    @DisplayName("consumer bean configuration")
    inner class ConditionalConfiguration {
        private val contextRunner = ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityAutoConfig::class.java, UserIpDetectionConsumerConfig::class.java))

        @Nested
        @DisplayName("when UserIpAddressEntityService is missing")
        inner class WhenUserIpAddressEntityServiceIsMissing {
            @Test
            @DisplayName("should not create consumer bean")
            fun notCreateConsumerBean() {
                contextRunner.run { context ->
                    assertThat(context).doesNotHaveBean(UserIpDetectionConsumerConfig::class.java)
                    assertThat(context).doesNotHaveBean("userIpDetection")
                    assertThat(context).doesNotHaveBean("userIpDetectionConsumer")
                }
            }
        }

        @Nested
        @DisplayName("when UserIpAddressEntityService is present")
        inner class WhenUserIpAddressEntityServiceIsPresent {
            @Test
            @DisplayName("should create consumer bean")
            fun createConsumerBean() {
                contextRunner
                    .withUserConfiguration(MockServiceConfig::class.java)
                    .run { context ->
                        assertThat(context).hasSingleBean(UserIpDetectionConsumerConfig::class.java)
                        assertThat(context).hasBean("userIpDetection")
                    }
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
