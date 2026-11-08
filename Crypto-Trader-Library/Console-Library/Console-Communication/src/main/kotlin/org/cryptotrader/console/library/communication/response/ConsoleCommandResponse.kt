package org.cryptotrader.console.library.communication.response

data class ConsoleCommandResponse(
    val consoleOutput: String,
    val payload: Any? = null,
    val isSuccessful: Boolean = true
) {
    override fun toString(): String = this.consoleOutput
}