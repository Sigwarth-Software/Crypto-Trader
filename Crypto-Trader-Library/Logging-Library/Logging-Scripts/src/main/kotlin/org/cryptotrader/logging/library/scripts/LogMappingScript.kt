package org.cryptotrader.logging.library.scripts

import jakarta.servlet.http.HttpServletRequest
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.logging.library.communication.request.FrontendLogErrorRequest
import org.cryptotrader.logging.library.communication.request.FrontendLogRequest
import org.cryptotrader.logging.library.events.FrontendLogEvent
import org.cryptotrader.universal.library.scripts.resolveIpAddress

fun mapToEvent(
    dto: FrontendLogRequest,
    request: HttpServletRequest,
    user: ProductUser?
): FrontendLogEvent {
    val error: FrontendLogErrorRequest? = dto.error
    return FrontendLogEvent(
        dto.timestamp,
        dto.level,
        dto.logger,
        dto.context,
        dto.message,
        dto.metadata,
        error?.name,
        error?.message,
        error?.stack,
        request.getHeader("x-client-app"),
        request.getHeader("User-Agent"),
        resolveIpAddress(request),
        request.remoteAddr,
        user
    )
}