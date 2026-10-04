package org.cryptotrader.api.library.communication.request

data class RangedPortfolioHistoryRequest(
    val startDate: String,
    val endDate: String
)
