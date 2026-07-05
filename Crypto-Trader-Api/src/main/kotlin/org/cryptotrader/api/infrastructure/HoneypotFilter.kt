package org.cryptotrader.api.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.cryptotrader.api.service.HoneypotService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class HoneypotFilter(
    private val honeypotService: HoneypotService
) : OncePerRequestFilter() {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(HoneypotFilter::class.java)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        if (this.honeypotService.captureHoneypot(request) != null) {
            val triggeredIp: String = request.remoteAddr
            log.warn("Honeypot triggered by IP: $triggeredIp, Path: ${request.requestURI}, Query: ${request.queryString}")
            response.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        chain.doFilter(request, response)
    }
}