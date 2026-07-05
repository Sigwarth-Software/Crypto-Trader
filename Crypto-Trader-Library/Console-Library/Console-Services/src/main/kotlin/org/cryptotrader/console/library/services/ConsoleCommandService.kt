package org.cryptotrader.console.library.services

import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.AuthContextService
import org.cryptotrader.console.library.communication.request.ConsoleCommandRequest
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.entity.ConsoleCommandExecution
import org.cryptotrader.console.library.model.ConsoleHelpCommand
import org.cryptotrader.console.library.model.ConsoleHelpSection
import org.cryptotrader.console.library.infrastructure.annotation.CommandHelp
import org.cryptotrader.console.library.model.SupportedConsoleCommand.*
import org.cryptotrader.console.library.model.exception.CommandNotSupportedException
import org.cryptotrader.console.library.scripts.formatConsoleHelp
import org.cryptotrader.console.library.services.entity.ConsoleCommandExecutionEntityService
import org.cryptotrader.console.library.services.models.ConsoleCommandExecutor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.aop.support.AopUtils
import org.springframework.stereotype.Service

@Service
class ConsoleCommandService @Autowired constructor(
    private val commandParser: ConsoleCommandParser,
    private val currencyCommandService: CurrencyCommandService,
    private val portfolioCommandService: PortfolioCommandService,
    private val commandExecutors: List<ConsoleCommandExecutor>,
    private val commandExecutionEntityService: ConsoleCommandExecutionEntityService,
    private val authContextService: AuthContextService
    ) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConsoleCommandService::class.java)
    }

    fun executeCommand(command: ConsoleCommandRequest): ConsoleCommandResponse {
        val defaultResponse =
            ConsoleCommandResponse("Unknown command: ${command.commandText}")
        var resolvedCommandName: String? = null
        val response: ConsoleCommandResponse = try {
            val parsedCommand = this.commandParser.parseCommand(command.commandText)
            resolvedCommandName = parsedCommand.name
            when (parsedCommand) {
                HELP -> ConsoleCommandResponse(this.buildHelpText())
                CURRENCY -> this.currencyCommandService.executeCommand(command.commandText)
                PORTFOLIO -> this.portfolioCommandService.executeCommand(command.commandText)
                AUTH -> TODO()
                TRADE -> TODO()
                TRADER -> TODO()
            }
        } catch (exception: CommandNotSupportedException) {
            log.error("Unsupported command entered: ${exception.message}")
            this.recordCommandExecution(
                command, resolvedCommandName, false, exception.message
            )
            return defaultResponse
        } catch (exception: Exception) {
            this.recordCommandExecution(
                command, resolvedCommandName, false, exception.message
            )
            throw exception
        }

        this.recordCommandExecution(
            command, resolvedCommandName, true, null
        )
        return response
    }

    private fun recordCommandExecution(
        command: ConsoleCommandRequest,
        resolvedCommandName: String?,
        successful: Boolean,
        errorMessage: String?
    ) {
        val productUser: ProductUser? = this.authContextService.getAuthenticatedProductUser()
        try {
            this.commandExecutionEntityService.save(
                ConsoleCommandExecution(
                    command.commandText,
                    resolvedCommandName,
                    successful,
                    productUser,
                    errorMessage
                )
            )
        } catch (exception: Exception) {
            log.error("Failed to persist console command execution: {}", command.commandText, exception)
        }
    }

    private fun buildHelpText(): String {
        val helpSections = this.commandExecutors.sortedBy { AopUtils.getTargetClass(it).simpleName }.mapNotNull { commandService ->
            val targetClass: Class<*> = AopUtils.getTargetClass(commandService)
            val helpAnnotation: CommandHelp = targetClass.getAnnotation(CommandHelp::class.java) ?: return@mapNotNull null
            val methodHelp: List<ConsoleHelpCommand> = targetClass.declaredMethods
                .filter { method -> method.isAnnotationPresent(CommandHelp::class.java) }
                .sortedBy { method ->
                    method.getAnnotation(CommandHelp::class.java).command
                }
                .map { method ->
                    val methodAnnotation = method.getAnnotation(CommandHelp::class.java)
                    ConsoleHelpCommand(
                        methodAnnotation.command, methodAnnotation.description
                    )
                }

            ConsoleHelpSection(
                helpAnnotation.command, helpAnnotation.description, methodHelp
            )
        }

        return formatConsoleHelp(helpSections)
    }
}
