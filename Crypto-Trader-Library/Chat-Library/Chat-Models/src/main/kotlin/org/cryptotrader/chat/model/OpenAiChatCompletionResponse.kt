package org.cryptotrader.chat.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiChatCompletionResponse(
    val id: String,
    @JsonProperty("object")
    val objectName: String = "chat.completion",
    val created: Long,
    val model: String,
    val choices: List<OpenAiChoice>,
    val usage: OpenAiUsage? = null
)