package org.cryptotrader.universal.library.scripts

import jakarta.servlet.http.HttpServletRequest

fun resolveIpAddress(request: HttpServletRequest): String? {
    val xForwardedFor: String? = request.getHeader(
        "X-Forwarded-For"
    )

    if (!xForwardedFor.isNullOrBlank()) {
        return xForwardedFor.split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()[0].trim { it <= ' ' }
    }
    return request.remoteAddr
}