package org.cryptotrader.console.library.model.command

import org.cryptotrader.console.library.model.ConsoleCommandRegex

enum class CurrencyCommand(
    override val regexString: String
) : ConsoleCommandRegex {
    TOP_LEVEL("""^currency$"""),
    LIST("""^currency list$"""),
    LIST_TOP("""^currency list --top \d+$"""),
    SHOW("""^currency show \S+$""")
}
