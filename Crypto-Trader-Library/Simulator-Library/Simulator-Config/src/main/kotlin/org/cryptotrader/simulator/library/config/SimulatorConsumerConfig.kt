package org.cryptotrader.simulator.library.config

import org.cryptotrader.simulator.library.communication.request.PortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.response.PortfolioSimulationResponse
import org.cryptotrader.simulator.library.events.SimulatorEventBinding
import org.cryptotrader.simulator.library.services.SimulatorService
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
open class SimulatorConsumerConfig @Autowired constructor(
    private val simulatorService: SimulatorService,
    streamBridge: StreamBridge
) : BaseEventConsumer<PortfolioSimulationRequest, PortfolioSimulationResponse>(
    streamBridge,
    SimulatorEventBinding.PORTFOLIO_SIMULATION_RESPONSES.bindingName
) {
    override val log: Logger = LoggerFactory.getLogger(SimulatorConsumerConfig::class.java)

    @Bean(name = ["simulatorConsumer"])
    open fun simulatorConsumer(): Consumer<Message<PortfolioSimulationRequest>> {
        return this.createConsumer { request, _ ->
            this.simulatorService.simulate(request)
        }
    }

    override fun handleError(exception: Exception): PortfolioSimulationResponse {
        return PortfolioSimulationResponse(0.0, 0.0)
    }
}
