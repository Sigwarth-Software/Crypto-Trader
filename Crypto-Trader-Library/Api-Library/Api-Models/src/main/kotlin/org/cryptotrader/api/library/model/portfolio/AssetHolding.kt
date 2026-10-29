package org.cryptotrader.api.library.model.portfolio

interface AssetHolding {
    fun isDollarsOnly(): Boolean
    fun isSharesOnly(): Boolean
}