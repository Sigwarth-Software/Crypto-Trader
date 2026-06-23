package org.cryptotrader.logging.library.events

import org.cryptotrader.api.library.entity.user.ProductUser
import java.time.LocalDateTime

open class FrontEndLogEventPayload(
    open val timestamp: LocalDateTime,
    open val level: String,
    open val logger: String,
    open val context: String?,
    open val message: String,
    open val metadata: Map<String, Any>?,
    open val errorName: String?,
    open val errorMessage: String?,
    open val errorStack: String?,
    open val clientApp: String?,
    open val userAgent: String?,
    open val ipAddress: String?,
    open val remoteAddress: String?,
    open val user: ProductUser?
)
