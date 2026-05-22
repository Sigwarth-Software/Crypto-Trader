package org.cryptotrader.api.library.services.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Verification
import org.cryptotrader.api.library.model.jwt.JwtClaims
import org.cryptotrader.api.library.services.rsa.RsaKeyService
import org.cryptotrader.universal.library.model.annotation.TimeTracked
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * Kotlin-native JWT service using RS256 signing with JWKS-ready kid header.
 * Falls back to ephemeral keys if none configured.
 */
/**
 * Issues and validates short-lived JWT access tokens.
 *
 * Claims:
 * - iss, aud, sub, iat, exp (TTL configurable via security.jwt.ttl-seconds, default 300s)
 * - email (auxiliary)
 * - cnf.jkt (optional): RFC7638 JWK thumbprint binding the token to a DPoP key
 *
 * Signing:
 * - RS256 with a stable kid provided by RsaKeyService; JWKS exposed by JwksController.
 */
@Service
class JwtTokenService(
    private val rsaKeyService: RsaKeyService,
    @param:Value("\${security.jwt.issuer:https://api.cryptotrader.com}") private val issuer: String,
    @param:Value("\${security.jwt.ttl-seconds:300}") private val ttlSeconds: Long,
    @param:Value("\${security.jwt.audience:crypto-trader-api}") private val audienceCsv: String
) {
    private val signAlgorithm: Algorithm = Algorithm.RSA256(null, this.rsaKeyService.privateKey)
    private val verifyAlgorithm: Algorithm = Algorithm.RSA256(this.rsaKeyService.publicKey, null)
    private val audiences: List<String> = this.audienceCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }

    companion object {
        private val log = LoggerFactory.getLogger(JwtTokenService::class.java)
    }

    /**
     * Create a short-lived access token (JWT).
     *
     * In plain words: we stamp who you are (subject), who the token is for (audience), when it was made (iat),
     * when it expires (exp), and—if available—the fingerprint of your browser key (cnf.jkt) to tie the token to you.
     *
     * @param subject the user id as a string
     * @param email   copied into a readable claim for convenience (not used for security decisions)
     * @param jwkThumbprint     optional key thumbprint to bind the token to a DPoP key
     * @return the signed JWT as a compact string
     */
    @JvmOverloads
    @TimeTracked(expectedMillis = 200, shouldPersist = true)
    fun generateToken(subject: String, email: String, jwkThumbprint: String? = null): String {
        val now: Instant = Instant.now()
        val expiresAt: Instant = now.plusSeconds(this.ttlSeconds)
        val jwtBuilder = JWT.create()
            .withIssuer(this.issuer)
            .withKeyId(this.rsaKeyService.kid)
            .withSubject(subject)
            .withClaim("email", email)
            .withIssuedAt(Date.from(now))
            .withJWTId(UUID.randomUUID().toString())
            .withExpiresAt(Date.from(expiresAt))
            .withAudience(*this.audiences.toTypedArray())
        if (!jwkThumbprint.isNullOrBlank()) {
            val jwtConfirmation = mapOf("jkt" to jwkThumbprint)
            jwtBuilder.withClaim("cnf", jwtConfirmation)
        }
        log.info("Generated JWT for {}.", subject)
        return jwtBuilder.sign(this.signAlgorithm)
    }

    /**
     * Validate and read an access token.
     *
     * What happens:
     * - We check the signature against our public key and verify expected issuer/audience/expiry.
     * - We then extract a few useful fields (user id, email, timestamps, and optional cnf.jkt binding).
     *
     * @param token the compact JWT string from the Authorization header
     * @return a simple Kotlin data object with the claims we care about
     * @throws com.auth0.jwt.exceptions.JWTVerificationException if the token is invalid/expired
     */
    @TimeTracked(expectedMillis = 200, shouldPersist = true)
    fun validateAndParse(token: String): JwtClaims {
        val verifierBuilder: Verification = JWT.require(this.verifyAlgorithm)
                                               .withIssuer(this.issuer)
        if (this.audiences.isNotEmpty()) {
            verifierBuilder.withAudience(*this.audiences.toTypedArray())
        }
        val verifiedJwt: DecodedJWT = verifierBuilder.build().verify(token)
        val subject: String? = verifiedJwt.subject
        val email: String? = verifiedJwt.getClaim("email").asString()
        val issuedAt: Date? = verifiedJwt.issuedAt
        val expiresAt: Date? = verifiedJwt.expiresAt
        val confirmationMap: Map<String?, Any?>? = verifiedJwt.getClaim("cnf")?.asMap()
        val jwkThumbprint: String? = confirmationMap?.get("jkt")?.toString()
        log.info("Validated JWT for {}.", email ?: "unknown user")
        return JwtClaims(subject, email, issuedAt, expiresAt, jwkThumbprint)
    }
}
