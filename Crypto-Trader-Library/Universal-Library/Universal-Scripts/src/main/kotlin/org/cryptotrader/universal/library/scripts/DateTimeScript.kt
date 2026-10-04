package org.cryptotrader.universal.library.scripts

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun toLocalDateTime(epochMillis: Long): LocalDateTime {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(epochMillis),
        ZoneId.systemDefault()
    )
}