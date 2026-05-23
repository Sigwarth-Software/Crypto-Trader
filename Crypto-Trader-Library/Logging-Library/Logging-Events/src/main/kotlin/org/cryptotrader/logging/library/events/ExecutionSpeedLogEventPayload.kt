package org.cryptotrader.logging.library.events

import java.time.LocalDateTime

data class ExecutionSpeedLogEventPayload(
    val executionSpeed: Long,
    val fullMethodQualifiedName: String,
    val methodName: String,
    val className: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val expectedExecutionSpeed: Long? = null,
)
