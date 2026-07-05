package org.cryptotrader.security.library.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.cryptotrader.security.library.service.model.IpBanManager
import org.cryptotrader.security.library.service.model.IpBanPolicy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.filter.OncePerRequestFilter

@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class IpBanFilter(
    private val ipBanManager: IpBanManager,
    private val blockResponseCode: Int
) : OncePerRequestFilter() {
    @Value("\${PSQL_HOST:localhost}")
    private val psqlHost: String? = null

    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)
    }

    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        val clientIp: String = request.remoteAddr
        if (IpBanPolicy.shouldBypass(clientIp)) {
            filterChain.doFilter(request, response)
            return
        }
        val trustedIp: String = this.psqlHost ?:
        System.getenv("PSQL_HOST")
        ?: throw IllegalStateException("PSQL_HOST environment variable is not set")

        // Bypass if it's our trusted host
        if (clientIp == trustedIp) {
            filterChain.doFilter(request, response)
            return
        }

        if (this.isClientBanned(clientIp)) {
            this.ipBanManager.recordAttempt(clientIp)
            log.warn("Blocked request from banned IP: $clientIp, Path: ${request.requestURI}, Query: ${request.queryString}")
            this.blockRequest(response)
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun isClientBanned(clientIp: String): Boolean {
        return this.ipBanManager.isBanned(clientIp)
    }

    private fun blockRequest(response: HttpServletResponse) {
        response.status = this.blockResponseCode
        response.flushBuffer()
    }
}
