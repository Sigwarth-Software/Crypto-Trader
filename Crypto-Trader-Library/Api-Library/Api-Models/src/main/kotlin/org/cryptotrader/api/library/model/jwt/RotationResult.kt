package org.cryptotrader.api.library.model.jwt

/**
 * Result of trying to use a refresh token.
 * - newRecord is present when rotation succeeded and should be set as the new cookie value.
 * - reuseDetected=true signals we saw an old/invalid token and revoked the session family.
 */
data class RotationResult(val newRecord: RefreshTokenRecord?, val reuseDetected: Boolean)