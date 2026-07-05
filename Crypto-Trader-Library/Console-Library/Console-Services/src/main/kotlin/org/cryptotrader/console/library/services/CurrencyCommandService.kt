package org.cryptotrader.console.library.services

import org.cryptotrader.api.library.communication.response.CurrencyNamesResponse
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.infrastructure.annotation.CommandHelp
import org.cryptotrader.console.library.model.command.CurrencyCommand
import org.cryptotrader.console.library.services.models.BaseConsoleCommandRunner
import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.data.library.services.CurrencyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@CommandHelp(
    command = "currency",
    description = "Currency commands"
)
@Service
class CurrencyCommandService @Autowired constructor(
    private val currencyService: CurrencyService
) : BaseConsoleCommandRunner<CurrencyCommand>(CurrencyCommand.TOP_LEVEL) {
    override fun executeResolvedCommand(command: CurrencyCommand, commandInput: String): ConsoleCommandResponse {
        return when (command) {
            CurrencyCommand.TOP_LEVEL, CurrencyCommand.LIST -> this.executeListCommand()
            CurrencyCommand.LIST_TOP -> this.executeTopListCommand(commandInput)
            CurrencyCommand.SHOW -> this.executeShowCommand(commandInput)
        }
    }


    @CommandHelp(
        command = "currency list [--top <number>]",
        description = "List available currencies or the top performers"
    )
    private fun executeListCommand(): ConsoleCommandResponse {
        val currencyNames: List<String> = this.currencyService.getCurrencyNames(true).sorted()
        val namesResponse = CurrencyNamesResponse(currencyNames)
        val consoleString = currencyNames.joinToString(separator = "\n") { it }
        return ConsoleCommandResponse(consoleString, namesResponse)
    }

    private fun executeTopListCommand(command: String): ConsoleCommandResponse {
        val topCount = try {
            this.parseTopCount(command)
        } catch (exception: IllegalArgumentException) {
            return ConsoleCommandResponse(exception.message ?: "Invalid command format.", null)
        }

        val currencyNames: List<String> = this.currencyService.getTopCurrenciesByPerformance(topCount)
        val namesResponse = CurrencyNamesResponse(currencyNames)
        val consoleString = currencyNames.joinToString(separator = "\n") { it }
        return ConsoleCommandResponse(consoleString, namesResponse)
    }

    @CommandHelp(
        command = "currency show <currency-code>",
        description = "Show a currency by code"
    )
    private fun executeShowCommand(command: String): ConsoleCommandResponse {
        val commandParts: List<String> = command.split(" ")
        if (commandParts.size != 3) {
            return ConsoleCommandResponse("Invalid command format.", null)
        }
        val currencyCode: String = commandParts[2].uppercase()
        val currency: Currency = this.currencyService.getCurrencyByCurrencyCode(currencyCode)
        return ConsoleCommandResponse(currency.toString(), currency)
    }

    private fun parseTopCount(command: String): Int {
        val commandParts = command.trim().split(Regex("\\s+"))
        if (commandParts.size != 4 || commandParts[2] != "--top") {
            throw IllegalArgumentException("Invalid command format.")
        }

        val topCount = commandParts[3].toIntOrNull() ?: throw IllegalArgumentException("Invalid command format.")
        if (topCount <= 0) {
            throw IllegalArgumentException("Invalid command format.")
        }
        return topCount
    }
}
