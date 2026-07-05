package org.cryptotrader.logging.library.events

import java.time.LocalDateTime

open class LogBatchEvent(
    open val entries: List<FrontEndLogEventPayload>,
    open val receivedAt: LocalDateTime
)
