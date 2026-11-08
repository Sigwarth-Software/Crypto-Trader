package org.cryptotrader.health.library.communication.request

import org.cryptotrader.health.library.model.CryptoTraderService
import org.cryptotrader.health.library.model.HealthStatusTimeRange

data class HealthStatusRequest(
    val module: CryptoTraderService,
    val timeRange: HealthStatusTimeRange
)
