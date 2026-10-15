package org.cryptotrader.chat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAiChatCompletionRequest(
    val model: String? = null,
    val messages: List<OpenAiMessage> = emptyList(),
    val stream: Boolean? = false,
    @JsonProperty("stream_options")
    val streamOptions: OpenAiStreamOptions? = null
)