package org.cryptotrader.simulator.library.events.publisher

import org.cryptotrader.simulator.library.communication.request.PortfolioSimulationRequest
import org.cryptotrader.simulator.library.events.SimulatorEventBinding
import org.cryptotrader.universal.library.events.EventPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SimulatorEventsPublisher @Autowired constructor(
    @Autowired(required = false)
    private val eventPublisher: EventPublisher?
) {
    private val log = LoggerFactory.getLogger(SimulatorEventsPublisher::class.java)

    fun publishEvent(binding: SimulatorEventBinding, simulationRequest: PortfolioSimulationRequest) {
        if (this.eventPublisher != null) {
            this.log.info("Publishing event {} to {}", simulationRequest, binding.bindingName)
            this.eventPublisher.publish(binding.bindingName, simulationRequest)
        } else {
            this.log.error("EventPublisher is null, cannot publish event")
        }
    }
}