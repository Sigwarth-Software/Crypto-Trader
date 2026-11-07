package org.cryptotrader.health.library.config.consumer

import org.cryptotrader.health.library.communication.request.HealthStatusRequest
import org.cryptotrader.health.library.communication.response.HealthStatusResponse
import org.cryptotrader.health.library.event.HealthEventBinding
import org.cryptotrader.health.library.service.HealthStatusService
import org.cryptotrader.universal.library.events.BaseEventConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration(proxyBeanMethods = false)
open class HealthStatusConsumerConfig @Autowired constructor(
    streamBridge: StreamBridge,
    private val healthStatusService: HealthStatusService
) : BaseEventConsumer<HealthStatusRequest, HealthStatusResponse>(
    streamBridge,
    HealthEventBinding.HEALTH_STATUS_RESPONSES.bindingName
) {
    override val log: Logger = LoggerFactory.getLogger(HealthStatusConsumerConfig::class.java)


    @Bean(name = ["healthStatusConsumer"])
    open fun healthStatusConsumer(): Consumer<Message<HealthStatusRequest>> {
        return this.createConsumer { request, _ ->
            log.info("Received health status request: {}", request)
            HealthStatusResponse(this.healthStatusService.getFromTimeRange(
                request.module,
                request.timeRange
            ))
        }
    }

    override fun handleError(exception: Exception): HealthStatusResponse {
        // If it fails, we want it to fail.
        throw exception
    }
}