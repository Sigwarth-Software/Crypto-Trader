package org.cryptotrader.simulator.library.communication.request

import java.time.LocalDateTime

open class PortfolioSimulationRequest(
    open val assetSimulationRequests: List<AssetSimulationRequest>,
    open val startDate: LocalDateTime,
    open val endDate: LocalDateTime,
)
