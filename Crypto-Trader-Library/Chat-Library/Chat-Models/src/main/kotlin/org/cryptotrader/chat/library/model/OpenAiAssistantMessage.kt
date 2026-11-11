package org.cryptotrader.chat.library.model

data class OpenAiAssistantMessage(
    val role: String = "assistant",
    val content: String = ""
)