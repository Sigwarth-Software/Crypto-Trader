package org.cryptotrader.console.library.services.module

import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.infrastructure.annotation.CommandHelp
import org.cryptotrader.console.library.model.command.HealthCommand
import org.cryptotrader.console.library.model.command.PortfolioCommand
import org.cryptotrader.console.library.services.models.BaseConsoleCommandRunner
import org.cryptotrader.health.library.model.CryptoTraderService
import org.cryptotrader.health.library.model.HealthStatusTimeRange
import org.cryptotrader.health.library.model.ServiceStatus
import org.cryptotrader.health.library.service.HealthStatusService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@CommandHelp(
    command = "health",
    description = "Health commands"
)
@Service
open class HealthCommandService(
    private val healthStatusService: HealthStatusService
) : BaseConsoleCommandRunner<HealthCommand>(HealthCommand.TOP_LEVEL) {

    companion object {
        val ALL_SERVICES: List<CryptoTraderService> = CryptoTraderService.nonLocalEntries().sortedBy { it.name }
    }

    @Transactional(readOnly = true)
    override fun executeResolvedCommand(
        command: HealthCommand,
        commandInput: String
    ): ConsoleCommandResponse {
        return when (command) {
            HealthCommand.TOP_LEVEL -> this.executeHealthCommand()
            HealthCommand.LIST -> TODO()
            HealthCommand.OVERVIEW -> TODO()
        }
    }


    @CommandHelp(
        command = "health",
        description = "Show the service health statuses"
    )
    private fun executeHealthCommand(): ConsoleCommandResponse {
        val rows: List<Pair<String, String>> = ALL_SERVICES.map { service ->
            service.toString() to this.healthStatusService
                .getAverageFromTimeRange(service, HealthStatusTimeRange.NOW)
                .toString()
        }

        val serviceWidth: Int = maxOf(
            "Service".length,
            rows.maxOfOrNull { it.first.length } ?: 0
        )

        val output: String = buildString {
            appendLine()
            appendLine("${"Service".padEnd(serviceWidth)} | Status")
            appendLine("${"-".repeat(serviceWidth)}-+-----------")

            rows.forEach { (service, status) ->
                appendLine("${service.padEnd(serviceWidth)} | $status")
            }
        }

        return ConsoleCommandResponse(output)
    }
}