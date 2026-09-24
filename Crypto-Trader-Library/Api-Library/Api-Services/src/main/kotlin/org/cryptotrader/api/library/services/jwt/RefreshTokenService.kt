package org.cryptotrader.api.library.services.jwt

import org.cryptotrader.api.library.model.jwt.RefreshTokenIssue
import org.cryptotrader.api.library.model.jwt.RefreshTokenRecord
import org.cryptotrader.api.library.model.jwt.RotationResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Refresh tokens, explained simply:
 * - Think of a refresh token as a long-lived “session receipt” stored in an HttpOnly cookie.
 * - Every time you use it, we swap it for a brand new one (so an old one can’t be reused).
 * - If an old token shows up again, we assume it was stolen and shut down the whole session family.
 *
 * This in-memory implementation is perfect for dev/single-node. Use Redis/DB for production.
 *
 * Key ideas:
 * - Each refresh token belongs to a family (one login session) and is bound to a DPoP key thumbprint (jkt).
 * - Rotate-on-use and reuse detection keep sessions safe.
 */
@Service
class RefreshTokenService(
    @Value("\${security.refresh.cookie-name:__Host-rt}") private val cookieName: String,
    @Value("\${security.refresh.ttl-days:30}") private val ttlDays: Long,
    @Value("\${security.refresh.allow-rebind-once:false}") private val allowRebindOnce: Boolean
) {
    private val records = ConcurrentHashMap<String, RefreshTokenRecord>()
    private val families = ConcurrentHashMap<String, MutableSet<String>>()
    private val familyRebindUsed = ConcurrentHashMap<String, Boolean>()

    fun cookieName(): String = this.cookieName

    /**
     * Start a new session: create a fresh refresh token record bound to the user (and DPoP key if present).
     * @param userId numeric id of the user
     * @param jwkThumbprint    optional DPoP key fingerprint to bind this session to the browser key
     * @return details used to set the cookie (id and expiry)
     */
    fun issue(userId: Long, jwkThumbprint: String?): RefreshTokenIssue {
        val now: Instant = Instant.now()
        val expiresAt: Instant = now.plus(Duration.ofDays(this.ttlDays))
        val familyId: String = UUID.randomUUID().toString()
        val id: String = UUID.randomUUID().toString()
        val record = RefreshTokenRecord(
            id = id,
            familyId = familyId,
            userId = userId,
            jkt = jwkThumbprint,
            expiresAt = expiresAt,
            used = false,
            revoked = false
        )
        this.records[id] = record
        this.families.computeIfAbsent(familyId) { mutableSetOf() }.add(id)
        this.familyRebindUsed.putIfAbsent(familyId, false)
        return RefreshTokenIssue(id, expiresAt, familyId)
    }

    /**
     * Validate the presented refresh token from cookie and rotate on success.
     *
     * @param presentedId  The opaque token id from the __Host-rt cookie.
     * @param presentedJkt The DPoP key thumbprint derived from the request's DPoP proof.
     * @return RotationResult with a new token record on success; null record indicates invalid/reuse and triggers family revocation.
     */
    /**
     * Use-then-rotate flow for refresh tokens.
     *
     * In plain words: check the cookie, make sure it belongs to this session and browser key, then swap it for a new one.
     * If an old/unknown token is presented, we revoke the whole family to cut off a possible theft.
     *
     * @param presentedId  the token id from the __Host-rt cookie
     * @param presentedJkt the jkt from the DPoP proof for this refresh request
     * @return RotationResult containing the new record on success; null record means invalid/reuse (family revoked)
     */
    fun validateAndRotate(presentedId: String, presentedJkt: String?): RotationResult {
        // Unknown id: cannot know family. Best-effort: reject as reuse attempt.
        val record = this.records[presentedId]
            ?: return RotationResult(null, reuseDetected = true)
        if (record.revoked || record.isExpired() || record.used) {
            this.revokeFamily(record.familyId)
            return RotationResult(null, reuseDetected = true)
        }
        // jkt must match (both null allowed)
        if (record.jkt != presentedJkt) {
            // Optional one-time rebind: accept a single jkt change per family when enabled
            val rebindUsed = this.familyRebindUsed.getOrDefault(record.familyId, false)
            if (this.allowRebindOnce && !rebindUsed && record.jkt.isNullOrBlank() && !presentedJkt.isNullOrBlank()) {
                // Allow a single rebind only from an unbound session (null/blank jkt) to a concrete jkt.
                this.familyRebindUsed[record.familyId] = true
                // Proceed with rotation but bind the new token to the new jkt
                record.used = true
                val newId = UUID.randomUUID().toString()
                val now = Instant.now()
                val expiresAt = now.plus(Duration.ofDays(this.ttlDays))
                val newRecord = record.copy(id = newId, jkt = presentedJkt, expiresAt = expiresAt, used = false, revoked = false)
                this.records[newId] = newRecord
                this.families.computeIfAbsent(record.familyId) { mutableSetOf() }.add(newId)
                return RotationResult(newRecord, reuseDetected = false)
            }
            this.revokeFamily(record.familyId)
            return RotationResult(null, reuseDetected = true)
        }
        // Rotate to a new token id
        record.used = true
        val newId = UUID.randomUUID().toString()
        val now = Instant.now()
        val expiresAt = now.plus(Duration.ofDays(this.ttlDays))
        val newRecord = record.copy(id = newId, expiresAt = expiresAt, used = false, revoked = false)
        this.records[newId] = newRecord
        this.families.computeIfAbsent(record.familyId) { mutableSetOf() }.add(newId)
        return RotationResult(newRecord, reuseDetected = false)
    }

    /**
     * Revoke every refresh token in a session family.
     * Use this when we detect reuse or other anomalies.
     */
    fun revokeFamily(familyId: String) {
        val tokenIds = this.families[familyId] ?: return
        for (tokenId in tokenIds) {
            this.records[tokenId]?.revoked = true
        }
        log.info("Refresh token family revoked: {} ({} tokens)", familyId, tokenIds.size)
    }

    fun clearExpired() {
        val iterator = this.records.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.isExpired()) {
                iterator.remove()
            }
        }
    }

    @Scheduled(fixedDelayString = "\${security.refresh.cleanup-interval-ms:900000}", initialDelayString = "\${security.refresh.cleanup-initial-delay-ms:60000}")
    fun scheduledClearExpired() {
        this.clearExpired()
    }

    /**
     * Revoke a session family by presenting one of its token ids.
     * Handy for logout when we read the cookie value.
     */
    fun revokeByTokenId(id: String) {
        val record = this.records[id] ?: return
        this.revokeFamily(record.familyId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(RefreshTokenService::class.java)
    }
}
