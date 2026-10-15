package org.cryptotrader.chat.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAiDelta(
    val role: String? = null,
    val content: String? = null
)