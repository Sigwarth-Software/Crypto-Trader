package org.cryptotrader.simulator.library.events

import org.cryptotrader.universal.library.events.model.EventBinding

enum class SimulatorEventBinding(override val bindingName: String) : EventBinding {
    PORTFOLIO_SIMULATION_REQUESTS("portfolioSimulationRequests-out-0"),
    PORTFOLIO_SIMULATION_RESPONSES("portfolioSimulationResponses-out-0"),

    DETAILED_PORTFOLIO_SIMULATION_REQUESTS("detailedPortfolioSimulationRequests-out-0"),
    DETAILED_PORTFOLIO_SIMULATION_RESPONSES("detailedPortfolioSimulationResponses-out-0"),

    ASSET_SIMULATION_REQUESTS("assetSimulationRequests-out-0"),
    ASSET_SIMULATION_RESPONSES("assetSimulationResponses-out-0")
}
