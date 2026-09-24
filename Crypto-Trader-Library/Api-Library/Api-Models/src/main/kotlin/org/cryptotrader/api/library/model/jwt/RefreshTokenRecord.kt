package org.cryptotrader.api.library.model.jwt

import java.time.Instant

/**
 * Server-side record for one refresh token in a family. This is not exposed to the browser.
 * Fields:
 * - id: opaque token identifier placed in the cookie
 * - familyId: groups a user’s session across rotations
 * - userId: owner of this session
 * - jkt: browser key fingerprint this session is tied to (null for legacy)
 * - expiresAt: natural expiry time
 * - used: set to true once rotated to prevent reuse
 * - revoked: set to true when the session is invalidated
 */
data class RefreshTokenRecord(
    val id: String,
    val familyId: String,
    val userId: Long,
    val jkt: String?,
    val expiresAt: Instant,
    var used: Boolean,
    var revoked: Boolean
) {
    fun isExpired(): Boolean = Instant.now().isAfter(this.expiresAt)
}