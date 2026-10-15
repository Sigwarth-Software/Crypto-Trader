package org.cryptotrader.chat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiStreamOptions(
    @JsonProperty("include_usage")
    val includeUsage: Boolean? = null
)