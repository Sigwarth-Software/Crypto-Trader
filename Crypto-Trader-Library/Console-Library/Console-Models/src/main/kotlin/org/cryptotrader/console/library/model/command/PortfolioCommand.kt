package org.cryptotrader.console.library.model.command

import org.cryptotrader.console.library.model.ConsoleCommandRegex

enum class PortfolioCommand(
    override val regexString: String
) : ConsoleCommandRegex {
    TOP_LEVEL("""^portfolio$"""),
    SHOW("""^portfolio show$""")
}
