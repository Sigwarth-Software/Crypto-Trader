package org.cryptotrader.security.library.config

import org.cryptotrader.security.library.model.properties.Encryption
import org.cryptotrader.security.library.model.properties.Http
import org.cryptotrader.security.library.model.properties.Bans
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security")
data class SecurityPropertiesConfig(
    val bans: Bans = Bans(),
    val http: Http = Http(),
    val crypto: Encryption = Encryption(),
    val encryption: Encryption? = null,
)
