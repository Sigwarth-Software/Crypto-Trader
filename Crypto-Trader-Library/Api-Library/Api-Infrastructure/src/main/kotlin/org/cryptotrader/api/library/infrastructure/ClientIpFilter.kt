package org.cryptotrader.api.library.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.ProductUserService
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.security.library.event.publisher.SecurityEventsPublisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class ClientIpFilter @Autowired constructor(
    private val securityEventsPublisher: SecurityEventsPublisher,
    private val userService: ProductUserService
) : OncePerRequestFilter() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ClientIpFilter::class.java)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val clientIp: String = request.remoteAddr
            val authentication: Authentication? =
                SecurityContextHolder.getContext()?.authentication
            val clientUser: ProductUser? =
                authentication?.principal as? ProductUser
            // If checked within the last minute, we skip publishing.
            val isNewUser: Boolean = clientUser?.lastIpCheckTimestamp == null
            val isWithinLastMinute: Boolean =
                !isNewUser && (clientUser.lastIpCheckTimestamp?.plusMinutes(
                    1
                    // Minium time will never be after now, so it forces a check.
                ) ?: LocalDateTime.MIN) > LocalDateTime.now()

            if (clientUser != null && clientIp.isNotBlank() && (isNewUser || !isWithinLastMinute)) {
                clientUser.lastIpCheckTimestamp = LocalDateTime.now()
                this.userService.saveUser(clientUser)

                this.securityEventsPublisher.publish(
                    UserIpDetectionEvent(
                        clientIp,
                        clientUser
                    )
                )
            }
        } catch (ex: Exception) {
            log.warn("Failed to publish UserIpDetectionEvent: {}", ex.toString(), ex)
        }

        filterChain.doFilter(request, response)
    }

}