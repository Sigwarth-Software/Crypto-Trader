package org.cryptotrader.console.library.model

enum class SupportedConsoleCommand(val commandBase: String) {
    AUTH("auth"),
    CURRENCY("currency"),
    HEALTH("health"),
    HELP("help"),
    PORTFOLIO("portfolio"),
    TRADE("trade"),
    TRADER("trader");
}