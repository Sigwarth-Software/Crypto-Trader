package org.cryptotrader.logging.library.events

import java.time.LocalDateTime

data class ApplicationExceptionEventPayload(
    val timestamp: LocalDateTime,
    val module: String,
    val logger: String,
    val level: String,
    val threadName: String,
    val exceptionClass: String,
    val exceptionMessage: String?,
    val stackTrace: String,
    val rootCauseClass: String?,
    val rootCauseMessage: String?,
)
