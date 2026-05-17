package org.cryptotrader.simulator.library.events

import org.cryptotrader.simulator.library.communication.request.PortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.response.DetailedPortfolioSimulationResponse
import org.cryptotrader.universal.library.events.EventPublisher
import org.cryptotrader.universal.library.events.RequestGatewayController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class DetailedSimulatorRequestGateway @Autowired constructor(
    override val eventPublisher: EventPublisher,
) : RequestGatewayController<PortfolioSimulationRequest, DetailedPortfolioSimulationResponse>() {

    override val defaultTimeout: Duration = Duration.ofSeconds(120)
}
