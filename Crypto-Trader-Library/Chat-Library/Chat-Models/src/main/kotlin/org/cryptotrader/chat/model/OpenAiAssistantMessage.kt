package org.cryptotrader.chat.model

data class OpenAiAssistantMessage(
    val role: String = "assistant",
    val content: String = ""
)