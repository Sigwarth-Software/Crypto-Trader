package org.cryptotrader.health.library.event

import org.cryptotrader.health.library.communication.request.HealthStatusRequest
import org.cryptotrader.health.library.communication.response.HealthStatusResponse
import org.cryptotrader.universal.library.events.EventPublisher
import org.cryptotrader.universal.library.events.RequestGatewayController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class HealthStatusRequestGateway @Autowired constructor(
    override val eventPublisher: EventPublisher
) : RequestGatewayController<HealthStatusRequest, HealthStatusResponse>()