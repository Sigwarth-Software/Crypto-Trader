package org.cryptotrader.security.library.model.properties

data class Tink(
    val keysetName: String = "default-aead",
    val generateIfMissing: Boolean = true
)