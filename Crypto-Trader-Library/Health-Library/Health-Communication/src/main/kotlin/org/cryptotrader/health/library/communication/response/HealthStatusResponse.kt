package org.cryptotrader.health.library.communication.response

import org.cryptotrader.health.library.model.ServiceStatus

data class HealthStatusResponse(
    val healthStatuses: List<ServiceStatus>
)
