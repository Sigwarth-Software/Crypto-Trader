package org.cryptotrader.chat.library.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiChoice(
    val index: Int,
    val message: OpenAiAssistantMessage,
    @JsonProperty("finish_reason")
    val finishReason: String?
)