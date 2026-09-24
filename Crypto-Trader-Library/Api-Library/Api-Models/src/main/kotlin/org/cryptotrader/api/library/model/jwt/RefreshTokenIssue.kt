package org.cryptotrader.api.library.model.jwt

import java.time.Instant

/**
 * Info needed to set the refresh cookie on the response.
 * id goes into the cookie value; expiresAt is used to compute Max-Age; familyId groups a session.
 */
data class RefreshTokenIssue(val id: String, val expiresAt: Instant, val familyId: String)