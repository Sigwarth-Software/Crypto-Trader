package org.cryptotrader.logging.library.entity

enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    UNKNOWN;

    companion object {
        @JvmStatic
        fun fromLevelName(levelName: String?): LogLevel =
            entries.firstOrNull { it.name.equals(levelName, ignoreCase = true) } ?: UNKNOWN
    }
}
