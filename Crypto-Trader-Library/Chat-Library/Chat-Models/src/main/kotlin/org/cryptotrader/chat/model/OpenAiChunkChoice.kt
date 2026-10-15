package org.cryptotrader.chat.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiChunkChoice(
    val index: Int,
    val delta: OpenAiDelta,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonProperty("finish_reason")
    val finishReason: String?
)