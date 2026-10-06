package org.cryptotrader.security.library.config

import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.security.library.service.entity.UserIpAddressEntityService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(UserIpAddressEntityService::class)
open class UserIpDetectionConsumerConfig @Autowired constructor(
    private val userIpAddressEntityService: UserIpAddressEntityService
) {
    companion object {
        private val log = LoggerFactory.getLogger(UserIpDetectionConsumerConfig::class.java)
    }

    @Bean(name = ["userIpDetection", "userIpDetectionConsumer"])
    @ConditionalOnMissingBean(name = ["userIpDetection", "userIpDetectionConsumer"])
    open fun userIpDetection(): Consumer<Message<UserIpDetectionEvent>> {
        return Consumer { message ->
            val event = message.payload
            log.debug("Received UserIpDetectionEvent for user {} with IP {}",
                event.productUser.id, event.ip)
            try {
                val user: ProductUser = event.productUser
                val ip: String = event.ip
                if (ip.isNotBlank()) {
                    this.userIpAddressEntityService.recordUserIp(user, ip)
                }
            } catch (ex: Exception) {
                log.warn("Failed to record user IP address: {}", ex.toString(), ex)
            }
        }
    }
}
