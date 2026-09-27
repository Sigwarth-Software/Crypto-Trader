package org.cryptotrader.logging.library.events

import java.time.LocalDateTime

data class ApplicationLogEventPayload(
    val timestamp: LocalDateTime,
    val level: String,
    val logger: String,
    val module: String,
    val threadName: String,
    val message: String,
    val mdcContext: Map<String, String>? = null,
    val errorName: String? = null,
    val errorMessage: String? = null,
    val errorStack: String? = null,
)
