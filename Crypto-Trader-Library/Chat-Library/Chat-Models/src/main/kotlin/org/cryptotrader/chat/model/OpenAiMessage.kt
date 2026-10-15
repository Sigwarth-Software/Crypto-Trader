package org.cryptotrader.chat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAiMessage(
    val role: String,
    val content: Any? = null,
    val name: String? = null,
    @JsonProperty("tool_call_id")
    val toolCallId: String? = null
)