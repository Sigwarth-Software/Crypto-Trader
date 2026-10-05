package org.cryptotrader.security.library.event

import com.fasterxml.jackson.annotation.JsonProperty
import org.cryptotrader.api.library.entity.user.ProductUser

data class UserIpDetectionEvent(
    @JsonProperty("ip")
    val ip: String = "",
    @JsonProperty("productUser")
    val productUser: ProductUser
)
