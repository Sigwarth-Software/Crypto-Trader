package org.cryptotrader.simulator.library.config

import org.cryptotrader.simulator.library.communication.request.DetailedPortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.response.DetailedPortfolioSimulationResponse
import org.cryptotrader.simulator.library.events.SimulatorEventBinding
import org.cryptotrader.simulator.library.services.SimulatorService
import org.cryptotrader.universal.library.events.BaseEventConsumer
import org.cryptotrader.universal.library.communication.response.CompareTimeValueResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration(proxyBeanMethods = false)
open class DetailedSimulatorConsumerConfig @Autowired constructor(
    private val simulatorService: SimulatorService,
    streamBridge: StreamBridge
) : BaseEventConsumer<DetailedPortfolioSimulationRequest, DetailedPortfolioSimulationResponse>(
    streamBridge,
    SimulatorEventBinding.DETAILED_PORTFOLIO_SIMULATION_RESPONSES.bindingName
) {
    override val log: Logger = LoggerFactory.getLogger(DetailedSimulatorConsumerConfig::class.java)

    @Bean(name = ["detailedSimulatorConsumer"])
    open fun detailedSimulatorConsumer(): Consumer<Message<DetailedPortfolioSimulationRequest>> {
        return this.createConsumer { request, _ ->
            this.simulatorService.detailedSimulate(request)
        }
    }

    override fun handleError(exception: Exception): DetailedPortfolioSimulationResponse {
        return DetailedPortfolioSimulationResponse(
            emptyList<CompareTimeValueResponse>(),
            0.0,
            0.0
        )
    }
}
