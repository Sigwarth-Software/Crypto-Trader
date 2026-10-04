package org.cryptotrader.development.library.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "github")
data class GitHubProperties(
    val token: String,
    val repository: String
)