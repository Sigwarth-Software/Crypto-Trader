package org.cryptotrader.api.library.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.security.library.event.publisher.SecurityEventsPublisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class ClientIpFilter @Autowired constructor(
    private val securityEventsPublisher: SecurityEventsPublisher
) : OncePerRequestFilter() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ClientIpFilter::class.java)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientIp: String = request.remoteAddr
        val authentication = SecurityContextHolder.getContext()?.authentication
        val clientUser: ProductUser? = authentication?.principal as? ProductUser

        if (clientUser != null && clientIp.isNotBlank()) {
            this.securityEventsPublisher.publish(
                UserIpDetectionEvent(
                    clientIp,
                    clientUser
                )
            )
        }

        filterChain.doFilter(request, response)
    }

}