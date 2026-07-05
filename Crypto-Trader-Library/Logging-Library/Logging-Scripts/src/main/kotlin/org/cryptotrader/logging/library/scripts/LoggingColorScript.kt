package org.cryptotrader.logging.library.scripts

import org.springframework.boot.ansi.AnsiColor

fun statusColor(status: Int): AnsiColor {
    if (status >= 500) return AnsiColor.RED
    if (status >= 400) return AnsiColor.YELLOW
    if (status >= 300) return AnsiColor.CYAN
    return AnsiColor.GREEN
}