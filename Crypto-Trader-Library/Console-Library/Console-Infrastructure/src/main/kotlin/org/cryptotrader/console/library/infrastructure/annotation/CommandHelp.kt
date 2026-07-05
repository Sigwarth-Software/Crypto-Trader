package org.cryptotrader.console.library.infrastructure.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHelp(
    val command: String,
    val description: String
)
