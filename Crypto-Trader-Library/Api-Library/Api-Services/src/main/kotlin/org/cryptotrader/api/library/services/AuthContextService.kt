package org.cryptotrader.api.library.services

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.jwt.JwtTokenService
import org.cryptotrader.api.library.model.jwt.JwtClaims
import org.cryptotrader.api.library.services.jwt.TokenBlacklistService
import org.cryptotrader.universal.library.extension.string.getBearerToken
import org.cryptotrader.universal.library.extension.string.getDpopToken
import org.cryptotrader.universal.library.extension.string.isBearer
import org.cryptotrader.universal.library.extension.string.isDpop
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Utilities for inspecting and mutating the current authentication context.
 *
 * - isAuthenticated(): true when a non-anonymous Authentication is present.
 * - logout(): best-effort blacklist of the presented access token and SecurityContext cleanup.
 */
@Service
class AuthContextService(
    private val jwtTokenService: JwtTokenService,
    private val tokenBlacklistService: TokenBlacklistService,
    private val productUserService: ProductUserService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(AuthContextService::class.java)
    }
    /**
     * True if the current request has a real logged-in user.
     * In other words: someone presented a valid access token and we recorded that user for this request.
     */
    fun isAuthenticated(): Boolean {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated &&
                authentication !is AnonymousAuthenticationToken
    }

    /**
     * Log the user out for this request by blacklisting the presented access token (if any)
     * until it would naturally expire, and clearing Spring Security’s context.
     * This does not delete the refresh cookie; the controller handles that.
     */
    fun logout() {
        try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            val request = requestAttributes?.request
            val token: String? = request?.let { extractTokenFromRequest(it) }
            if (!token.isNullOrBlank()) {
                try {
                    val claims: JwtClaims = this.jwtTokenService.validateAndParse(token)
                    val expMillis: Long = claims.expiresAt?.time ?: 0L
                    this.tokenBlacklistService.blacklistToken(token, expMillis)
                    log.debug("Blacklisted token expiring at {}", expMillis)
                } catch (ex: Exception) {
                    log.debug("Logout: unable to parse token for blacklist; ignoring", ex)
                }
            } else {
                log.debug("Logout: No token found in Authorization header, X-Auth-Token, query param 'token', or cookies; nothing to blacklist")
            }
        } catch (ex: Exception) {
            log.debug("Logout: failed to extract token from request", ex)
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    fun getAuthenticatedProductUser(): ProductUser? {
        if (!this.isAuthenticated()) return null
        val auth: Authentication = SecurityContextHolder.getContext().authentication ?: return null
        return when (val principal = auth.principal) {
            is ProductUser -> principal
            is UserDetails -> {
                val username: String = principal.username
                runCatching { this.productUserService.getUserByEmail(username) }.getOrNull()
                    ?: runCatching { this.productUserService.getUserByUsername(username) }.getOrNull()
            }
            is String -> {
                runCatching { this.productUserService.getUserByEmail(principal) }.getOrNull()
                    ?: runCatching { this.productUserService.getUserByUsername(
                        principal
                    ) }.getOrNull()
            }
            else -> null
        }
    }

    fun isUserAuthenticated(): Boolean = this.getAuthenticatedProductUser() != null
    
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val authHeader: String? = request.getHeader("Authorization")
        val tokenFromHeader = authHeader?.let {
            when {
                it.isBearer() -> it.getBearerToken()
                it.isDpop() -> it.getDpopToken()
                else -> it
            }
        }
        if (!tokenFromHeader.isNullOrBlank()) return tokenFromHeader.trim()

        // 2) Alternate header
        val altHeader: String? = request.getHeader("X-Auth-Token")?.trim()
        if (!altHeader.isNullOrBlank()) return altHeader

        // 3) Query parameter
        val paramToken: String? = request.getParameter("token")?.trim()
        if (!paramToken.isNullOrBlank()) return paramToken

        // 4) Cookies (Authorization, authToken, jwt, token)
        val cookies: Array<out Cookie?>? = request.cookies
        if (cookies != null) {
            val names: Set<String> = setOf("Authorization", "authToken", "jwt", "token")
            for (cookie: Cookie? in cookies) {
                if (names.contains(cookie?.name)) {
                    val cookieValue = cookie?.value?.trim()
                    if (!cookieValue.isNullOrBlank()) {
                        return when {
                            cookieValue.isBearer() -> cookieValue.getBearerToken()
                            cookieValue.isDpop() -> cookieValue.getDpopToken()
                            else -> cookieValue
                        }
                    }
                }
            }
        }
        return null
    }
}