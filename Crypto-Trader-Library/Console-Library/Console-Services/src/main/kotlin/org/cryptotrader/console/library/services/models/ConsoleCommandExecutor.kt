package org.cryptotrader.console.library.services.models

import org.cryptotrader.console.library.communication.request.ConsoleCommandRequest
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.model.ConsoleCommandRegex
import org.cryptotrader.console.library.model.exception.CommandNotSupportedException
import kotlin.jvm.Throws

interface ConsoleCommandExecutor {
    fun isTopLevelCommand(commandInput: String): Boolean
    fun isTopLevelCommand(commandInput: ConsoleCommandRequest): Boolean

    fun isCommandMatch(command: ConsoleCommandRegex, commandInput: String): Boolean
    fun isCommandMatch(command: ConsoleCommandRegex, commandInput: ConsoleCommandRequest): Boolean

    @Throws(CommandNotSupportedException::class)
    fun resolveCommand(commandInput: String): ConsoleCommandRegex

    @Throws(CommandNotSupportedException::class)
    fun resolveCommand(commandInput: ConsoleCommandRequest): ConsoleCommandRegex

    @Throws(CommandNotSupportedException::class)
    fun executeCommand(commandInput: String): ConsoleCommandResponse

    @Throws(CommandNotSupportedException::class)
    fun executeCommand(commandInput: ConsoleCommandRequest): ConsoleCommandResponse

    @Throws(CommandNotSupportedException::class)
    fun executeCommand(command: ConsoleCommandRegex, commandInput: String): ConsoleCommandResponse

    @Throws(CommandNotSupportedException::class)
    fun executeCommand(command: ConsoleCommandRegex, commandInput: ConsoleCommandRequest): ConsoleCommandResponse
}