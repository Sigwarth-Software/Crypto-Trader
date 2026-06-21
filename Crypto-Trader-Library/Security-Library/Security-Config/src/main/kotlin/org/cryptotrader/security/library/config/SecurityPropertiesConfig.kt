package org.cryptotrader.security.library.config

import org.springframework.boot.context.properties.ConfigurationProperties

// TODO: Clean up and migrate code.
@ConfigurationProperties(prefix = "security")
data class SecurityPropertiesConfig(
    val bans: Bans = Bans(),
    val http: Http = Http(),
    val crypto: Encryption = Encryption(),
    val encryption: Encryption? = null,
) {
    data class Bans(
        val enabled: Boolean = true,
        val denylist: List<String> = emptyList(),
    )

    data class Http(
        val blockResponseCode: Int = 404
    )

    data class Encryption(
        val tink: Tink = Tink()
    ) {
        data class Tink(
            val keysetName: String = "default-aead",
            val generateIfMissing: Boolean = true
        )
    }
}
