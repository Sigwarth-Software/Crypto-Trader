package org.cryptotrader.simulator.library.communication.request

import org.cryptotrader.simulator.library.model.DownsampleDuration
import java.time.LocalDateTime

data class DetailedPortfolioSimulationRequest(
    override val assetSimulationRequests: List<AssetSimulationRequest>,
    override val startDate: LocalDateTime,
    override val endDate: LocalDateTime,
    val downsampleDuration: DownsampleDuration
) : PortfolioSimulationRequest(
    assetSimulationRequests,
    startDate,
    endDate
)
