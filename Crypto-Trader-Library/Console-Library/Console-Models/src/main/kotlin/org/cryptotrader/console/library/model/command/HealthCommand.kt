package org.cryptotrader.console.library.model.command

import org.cryptotrader.console.library.model.ConsoleCommandRegex

enum class HealthCommand(
    override val regexString: String
) : ConsoleCommandRegex {
    TOP_LEVEL("""^health$"""),
    LIST("""^health list$"""),
    OVERVIEW("""^health overview$""")
}