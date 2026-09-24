package org.cryptotrader.api.library.model.dpop

data class DpopVerificationResult(
    val jwkThumbprint: String,
    val jwtId: String?
)