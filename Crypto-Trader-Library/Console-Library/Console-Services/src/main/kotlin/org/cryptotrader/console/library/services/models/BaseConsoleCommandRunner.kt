package org.cryptotrader.console.library.services.models

import org.cryptotrader.console.library.communication.request.ConsoleCommandRequest
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.model.ConsoleCommandRegex
import org.cryptotrader.console.library.model.exception.CommandNotSupportedException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseConsoleCommandRunner<TopLevelCommand>(
    private val topLevelCommand: TopLevelCommand
) : ConsoleCommandExecutor
    where TopLevelCommand : Enum<TopLevelCommand>,
          TopLevelCommand : ConsoleCommandRegex {
    private val commandEntries: Array<TopLevelCommand> by lazy {
        resolveCommandEntries()
    }

    override fun isTopLevelCommand(commandInput: String): Boolean {
        return this.topLevelCommand.matches(commandInput)
    }

    override fun isTopLevelCommand(commandInput: ConsoleCommandRequest): Boolean {
        return this.isTopLevelCommand(commandInput.commandText)
    }

    override fun isCommandMatch(command: ConsoleCommandRegex, commandInput: String): Boolean {
        return command.matches(commandInput)
    }

    override fun isCommandMatch(command: ConsoleCommandRegex, commandInput: ConsoleCommandRequest): Boolean {
        return this.isCommandMatch(command, commandInput.commandText)
    }

    override fun resolveCommand(commandInput: String): TopLevelCommand {
        return this.commandEntries
            .sortedByDescending { it.regexString.length }
            .firstOrNull { it.matches(commandInput) }
            ?: throw CommandNotSupportedException(commandInput)
    }

    override fun resolveCommand(commandInput: ConsoleCommandRequest): TopLevelCommand {
        return this.resolveCommand(commandInput.commandText)
    }

    protected abstract fun executeResolvedCommand(command: TopLevelCommand, commandInput: String): ConsoleCommandResponse

    override fun executeCommand(command: ConsoleCommandRegex, commandInput: String): ConsoleCommandResponse {
        @Suppress("UNCHECKED_CAST")
        val typedCommand = command as? TopLevelCommand
            ?: throw CommandNotSupportedException(commandInput)
        return this.executeResolvedCommand(typedCommand, commandInput)
    }

    override fun executeCommand(command: ConsoleCommandRegex, commandInput: ConsoleCommandRequest): ConsoleCommandResponse {
        return this.executeCommand(command, commandInput.commandText)
    }

    override fun executeCommand(commandInput: String): ConsoleCommandResponse {
        return this.executeResolvedCommand(this.resolveCommand(commandInput), commandInput)
    }

    override fun executeCommand(commandInput: ConsoleCommandRequest): ConsoleCommandResponse {
        return this.executeCommand(commandInput.commandText)
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveCommandEntries(): Array<TopLevelCommand> {
        val commandEnumClass = this.resolveCommandEnumClass()
        return commandEnumClass.enumConstants
            ?: throw IllegalStateException("Unable to resolve command entries for ${commandEnumClass.name}")
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveCommandEnumClass(): Class<TopLevelCommand> {
        var currentClass: Class<*>? = this.javaClass

        while (currentClass != null) {
            val genericSuperclass: Type = currentClass.genericSuperclass
            if (genericSuperclass is ParameterizedType) {
                val rawType: Class<*>? = genericSuperclass.rawType as? Class<*>
                if (rawType == null) {
                    currentClass = currentClass.superclass
                    continue
                }

                if (BaseConsoleCommandRunner::class.java.isAssignableFrom(rawType)) {
                    val actualTypeArgument: Type = genericSuperclass.actualTypeArguments.firstOrNull()
                        ?: break

                    val resolvedClass: Class<out Any>? = when (actualTypeArgument) {
                        is Class<*> -> actualTypeArgument
                        is ParameterizedType -> actualTypeArgument.rawType as? Class<*>
                        else -> null
                    }

                    if (resolvedClass != null && Enum::class.java.isAssignableFrom(resolvedClass)) {
                        return resolvedClass as Class<TopLevelCommand>
                    }
                }
            }

            currentClass = currentClass.superclass
        }

        throw IllegalStateException("Unable to resolve command enum type for ${this.javaClass.name}")
    }
}
