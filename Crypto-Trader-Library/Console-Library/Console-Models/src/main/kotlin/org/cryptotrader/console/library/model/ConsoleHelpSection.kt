package org.cryptotrader.console.library.model

data class ConsoleHelpSection(
    val command: String,
    val description: String,
    val subcommands: List<ConsoleHelpCommand> = emptyList()
)