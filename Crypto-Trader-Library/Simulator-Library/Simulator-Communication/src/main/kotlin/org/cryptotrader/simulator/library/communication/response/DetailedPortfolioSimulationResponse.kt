package org.cryptotrader.simulator.library.communication.response

import org.cryptotrader.universal.library.communication.response.CompareTimeValueResponse

data class DetailedPortfolioSimulationResponse(
    val points: List<CompareTimeValueResponse>,
    override val cryptoTraderProfit: Double,
    override val naturalProfit: Double,
) : PortfolioSimulationResponse(cryptoTraderProfit, naturalProfit) {

}
