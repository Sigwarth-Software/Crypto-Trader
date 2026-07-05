package org.cryptotrader.api.library.communication.response

data class SubscriptionTierPricesResponse(
    val freeTierCost: Double,
    val proTierCost: Double,
    val ultimateTierCost: Double,
    )
