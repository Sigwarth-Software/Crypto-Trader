package org.cryptotrader.security.library.model.properties

data class Bans(
    val enabled: Boolean = true,
    val denylist: List<String> = emptyList(),
)