package org.cryptotrader.logging.library.events

import org.cryptotrader.api.library.entity.user.ProductUser
import java.time.LocalDateTime

class FrontendLogEvent(
    timestamp: LocalDateTime,
    level: String,
    logger: String,
    context: String?,
    message: String,
    metadata: Map<String, Any>?,
    errorName: String?,
    errorMessage: String?,
    errorStack: String?,
    clientApp: String?,
    userAgent: String?,
    ipAddress: String?,
    remoteAddress: String?,
    user: ProductUser?
) : FrontEndLogEventPayload(
    timestamp,
    level,
    logger,
    context,
    message,
    metadata,
    errorName,
    errorMessage,
    errorStack,
    clientApp,
    userAgent,
    ipAddress,
    remoteAddress,
    user
)
