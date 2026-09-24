package org.cryptotrader.api.library.infrastructure.dpop

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.filter.OncePerRequestFilter
import org.cryptotrader.api.library.model.dpop.DpopProofContext
import com.auth0.jwt.JWT
import org.cryptotrader.api.library.component.dpop.DpopReplayCache
import org.cryptotrader.api.library.infrastructure.extension.getAuthHeader
import org.cryptotrader.api.library.infrastructure.extension.getBearerToken
import org.cryptotrader.api.library.infrastructure.extension.getDpopHeader
import org.cryptotrader.api.library.infrastructure.extension.getDpopToken
import org.cryptotrader.api.library.infrastructure.extension.isBearer
import org.cryptotrader.api.library.infrastructure.extension.isDpop
import org.cryptotrader.api.library.services.jwt.JwtTokenService
import org.cryptotrader.api.library.model.jwt.JwtClaims
import org.cryptotrader.api.library.extension.sendUnauthorized
import org.cryptotrader.api.library.extension.fullUrl
import org.cryptotrader.api.library.model.dpop.DpopVerificationResult
import org.cryptotrader.api.library.services.dpop.DpopVerifierService

/**
 * Filter for DPoP proof validation before JWT authentication.
 */
@ConditionalOnProperty(name = ["security.auth.dpop.enabled"], havingValue = "true", matchIfMissing = true)
@Component
class DpopValidationFilter(
    private val dpopVerifier: DpopVerifierService,
    private val replayCache: DpopReplayCache,
    private val jwtTokenService: JwtTokenService
) : OncePerRequestFilter() {

    companion object {
        private val log = LoggerFactory.getLogger(DpopValidationFilter::class.java)
        const val REQ_ATTR_PROOF: String = "dpop.proof"
        const val REQ_ATTR_PROOF_JKT: String = "dpop.proof.jkt"
    }

    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        // Allow unauthenticated status check without DPoP enforcement
        val uriString: String = request.requestURI ?: ""
        if (this.isException(uriString)) {
            filterChain.doFilter(request, response)
            return
        }
        val authorizationHeader: String? = request.getAuthHeader()
        val isDpopAuth: Boolean = authorizationHeader?.isDpop() ?: false
        if (!isDpopAuth) {
            // Enforce DPoP usage for DPoP-bound tokens (those carrying cnf.jkt)
            val bearerHeader: String? = request.getAuthHeader()
            if (bearerHeader?.isBearer() ?: false) {
                val accessToken: String = bearerHeader.getBearerToken()
                try {
                    val claims: JwtClaims = this.jwtTokenService.validateAndParse(accessToken)
                    if (!claims.confirmJkt.isNullOrBlank()) {
                        log.debug("Bearer token with cnf.jkt used without DPoP proof; rejecting")
                        response.sendUnauthorized("dpop_required", "DPoP proof required for bound token", "DPoP")
                        return
                    }
                } catch (_: Exception) {
                    // Let downstream filters handle invalid/expired tokens
                }
            }
            filterChain.doFilter(request, response)
            return
        }

        val accessToken: String = authorizationHeader.getDpopToken()
        val dpopHeader: String? = request.getDpopHeader()
        if (dpopHeader.isNullOrBlank()) {
            log.debug("DPoP header missing while using DPoP authorization scheme")
            response.sendUnauthorized("invalid_dpop", "Missing DPoP proof", "DPoP")
            return
        }

        try {
            // Verify DPoP proof cryptographically and validate claims (require 'ath' when token provided)
            val verification: DpopVerificationResult? =
                this.dpopVerifier.verify(dpopHeader, request.method, request.fullUrl(), accessToken)
            if (verification == null) {
                log.debug("DPoP proof verification failed")
                response.sendUnauthorized("invalid_dpop", "Verification failed", "DPoP")
                return
            }
            // Replay detection by jti
            if (this.replayCache.isReplay(verification.jwtId)) {
                log.debug("DPoP proof replay detected for jti={}", verification.jwtId)
                response.sendUnauthorized("replay", "DPoP proof replay detected", "DPoP")
                return
            }

            val issuedAt: Long = try {
                JWT.decode(dpopHeader).getClaim("iat").asLong() ?: 0L
            } catch (_: Exception) {
                0L
            }
            val dpopProofContext = DpopProofContext(
                keyThumbprint = verification.jwkThumbprint,
                proofNonce = verification.jwtId,
                httpMethod = request.method,
                httpUrl = request.fullUrl(),
                issuedAtEpochSeconds = issuedAt
            )
            request.setAttribute(REQ_ATTR_PROOF, dpopProofContext)
            request.setAttribute(REQ_ATTR_PROOF_JKT, verification.jwkThumbprint)
        } catch (ex: Exception) {
            log.debug("DPoP validation failed", ex)
            response.sendUnauthorized("invalid_dpop", "Validation exception", "DPoP")
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun isException(uriString: String): Boolean =
        uriString.startsWith("/api/auth/logout") ||
                uriString.endsWith("/api/auth/logged-in")
}