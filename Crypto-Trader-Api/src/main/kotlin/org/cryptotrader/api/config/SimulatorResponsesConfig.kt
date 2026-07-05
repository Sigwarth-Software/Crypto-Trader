package org.cryptotrader.api.config

import org.cryptotrader.simulator.library.communication.request.PortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.response.PortfolioSimulationResponse
import org.cryptotrader.simulator.library.events.SimulatorRequestGateway
import org.cryptotrader.universal.library.events.config.BaseGatewayConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration
open class SimulatorResponsesConfig(
    gateway: SimulatorRequestGateway
) : BaseGatewayConfig<PortfolioSimulationRequest, PortfolioSimulationResponse>(gateway) {
    override val log: Logger = LoggerFactory.getLogger(SimulatorResponsesConfig::class.java)

    @Bean(name = ["portfolioSimulationResponsesConsumer"])
    open fun portfolioSimulationResponsesConsumer(): Consumer<Message<PortfolioSimulationResponse>> {
        return this.createConsumer()
    }
}
