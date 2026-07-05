package org.cryptotrader.console.library.scripts

import org.cryptotrader.console.library.model.ConsoleHelpSection

const val COLUMN_GAP = 4

fun formatConsoleHelp(sections: List<ConsoleHelpSection>): String {
    if (sections.isEmpty()) {
        return "No command help is available."
    }
    return sections.joinToString(separator = "\n\n") { section ->
        val allCommands: List<String> = listOf(section.command) + section.subcommands.map { it.command }
        val commandColumnWidth: Int = allCommands.maxOf { it.length }

        buildList {
            add(formatConsoleHelpLine(section.command, section.description, commandColumnWidth, COLUMN_GAP))
            section.subcommands.forEach { subcommand ->
                add(
                    formatConsoleHelpLine(
                        command = subcommand.command,
                        description = subcommand.description,
                        commandColumnWidth = commandColumnWidth,
                        columnGap = COLUMN_GAP,
                        indent = "  "
                    )
                )
            }
        }.joinToString(separator = "\n")
    }
}

private fun formatConsoleHelpLine(
    command: String,
    description: String,
    commandColumnWidth: Int,
    columnGap: Int,
    indent: String = ""
): String {
    return buildString {
        append(indent)
        append(command.padEnd(commandColumnWidth + columnGap))
        append(description)
    }
}
