package org.cryptotrader.simulator.library.communication.request

data class AssetSimulationRequest(
    val currencyCode: String,
    val numShares: Double,
    val numDollars: Double,
    )
