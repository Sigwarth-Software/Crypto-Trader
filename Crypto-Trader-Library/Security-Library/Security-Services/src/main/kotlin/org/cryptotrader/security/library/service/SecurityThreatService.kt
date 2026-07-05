package org.cryptotrader.security.library.service

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.cryptotrader.security.library.service.model.IpBanPolicy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SecurityThreatService(
    private val ipBanService: IpBanService,
    private val blockResponseCode: Int = 404
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(javaClass)
    }
    fun handle(request: HttpServletRequest, response: HttpServletResponse, next: () -> Unit) {
        val clientIp: String = request.remoteAddr
        if (IpBanPolicy.shouldBypass(clientIp)) {
            next()
            return
        }
        if (this.isClientBlocked(clientIp)) {
            this.ipBanService.recordAttempt(clientIp)
            log.warn("Blocked request from banned IP: $clientIp, Path: ${request.requestURI}, Query: ${request.queryString}")
            this.blockRequest(response)
            return
        }
        next()
    }

    fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        this.handle(request, response) {
            chain.doFilter(request, response)
        }
    }

    private fun isClientBlocked(clientIp: String): Boolean {
        return this.ipBanService.isBanned(clientIp)
    }

    private fun blockRequest(response: HttpServletResponse) {
        response.status = this.blockResponseCode
        response.flushBuffer()
    }
}
