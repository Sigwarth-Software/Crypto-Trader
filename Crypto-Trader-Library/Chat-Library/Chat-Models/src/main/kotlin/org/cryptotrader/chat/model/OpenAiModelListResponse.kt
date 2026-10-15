package org.cryptotrader.chat.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenAiModelListResponse(
    @JsonProperty("object")
    val objectName: String = "list",
    val data: List<OpenAiModel>
)