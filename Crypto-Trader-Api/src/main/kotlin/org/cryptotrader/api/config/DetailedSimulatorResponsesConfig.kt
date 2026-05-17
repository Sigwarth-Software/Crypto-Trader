package org.cryptotrader.api.config

import org.cryptotrader.simulator.library.communication.request.PortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.response.DetailedPortfolioSimulationResponse
import org.cryptotrader.simulator.library.events.DetailedSimulatorRequestGateway
import org.cryptotrader.universal.library.events.config.BaseGatewayConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import java.util.function.Consumer

@Configuration
open class DetailedSimulatorResponsesConfig(
    gateway: DetailedSimulatorRequestGateway
) : BaseGatewayConfig<PortfolioSimulationRequest, DetailedPortfolioSimulationResponse>(gateway) {
    override val log: Logger = LoggerFactory.getLogger(DetailedSimulatorResponsesConfig::class.java)

    @Bean(name = ["detailedPortfolioSimulationResponsesConsumer"])
    open fun detailedPortfolioSimulationResponsesConsumer(): Consumer<Message<DetailedPortfolioSimulationResponse>> {
        return this.createConsumer()
    }
}
