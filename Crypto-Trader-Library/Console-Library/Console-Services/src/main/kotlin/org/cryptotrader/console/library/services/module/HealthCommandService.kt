package org.cryptotrader.console.library.services.module

import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.infrastructure.annotation.CommandHelp
import org.cryptotrader.console.library.model.command.HealthCommand
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
            HealthCommand.LIST -> this.executeHealthListCommand()
            HealthCommand.OVERVIEW -> this.executeHealthOverviewCommand(commandInput)
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

    @CommandHelp(
        command = "health list",
        description = "Show the service\'s and available statuses"
    )
    private fun executeHealthListCommand(): ConsoleCommandResponse {
        val serviceStatuses: List<String> = ServiceStatus.entries.toList().map { it.toString() }

        val statusesString: String = "Available statuses:\n" +
            serviceStatuses.joinToString("\n") { "\t$it" }

        val servicesString: String = "Available services:\n" +
            ALL_SERVICES.joinToString("\n") { "\t$it" }

        val combinedString = "$statusesString\n\n$servicesString"
        return ConsoleCommandResponse(combinedString)
    }

    @CommandHelp(
        command = "health overview <module>",
        description = "Show the service\'s status"
    )
    private fun executeHealthOverviewCommand(commandInput: String): ConsoleCommandResponse {
        val commandWords: List<String> = commandInput.split(" ")
        val module: String = commandWords.getOrNull(2)
            ?: return ConsoleCommandResponse("Module not specified", isSuccessful = false)
        val service: CryptoTraderService
        try {
            service = CryptoTraderService.from(module)
        } catch (exception: IllegalArgumentException) {
            return ConsoleCommandResponse("Invalid module specified", isSuccessful = false)
        }

        val timeRanges: List<HealthStatusTimeRange> = HealthStatusTimeRange.entries.toList()

        val timeRangeStrings: MutableList<String> = mutableListOf()

        for (timeRange: HealthStatusTimeRange in timeRanges) {
            val status: ServiceStatus = this.healthStatusService.getAverageFromTimeRange(service,
                timeRange)
            timeRangeStrings.add("${timeRange.name.uppercase()}: $status")
        }

        val combinedString: String = timeRangeStrings.joinToString("\n")
        return ConsoleCommandResponse(combinedString)
    }
}
