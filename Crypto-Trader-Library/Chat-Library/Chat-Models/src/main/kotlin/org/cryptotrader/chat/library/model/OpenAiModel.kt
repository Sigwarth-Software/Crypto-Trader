package org.cryptotrader.chat.library.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiModel(
    val id: String,
    @JsonProperty("object")
    val objectName: String = "model",
    val created: Long,
    @JsonProperty("owned_by")
    val ownedBy: String = "openai"
)