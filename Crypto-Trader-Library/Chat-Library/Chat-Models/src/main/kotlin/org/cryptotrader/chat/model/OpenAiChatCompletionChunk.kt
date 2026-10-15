package org.cryptotrader.chat.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiChatCompletionChunk(
    val id: String,
    @JsonProperty("object")
    val objectName: String = "chat.completion.chunk",
    val created: Long,
    val model: String,
    val choices: List<OpenAiChunkChoice>,
    val usage: OpenAiUsage? = null
)