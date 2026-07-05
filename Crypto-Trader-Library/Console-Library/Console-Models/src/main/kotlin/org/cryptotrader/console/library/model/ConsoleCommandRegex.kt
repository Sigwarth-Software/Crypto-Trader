package org.cryptotrader.console.library.model

interface ConsoleCommandRegex {
    val regexString: String
    fun asRegex(): Regex = Regex(this.regexString)
    fun matches(input: String): Boolean = this.asRegex().matches(input)
}
