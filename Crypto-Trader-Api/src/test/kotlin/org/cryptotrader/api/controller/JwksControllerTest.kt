package org.cryptotrader.api.controller

import org.cryptotrader.api.library.services.rsa.RsaKeyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mock

@Tag("JwksController")
@Tag("controller")
@DisplayName("JWKS Controller")
class JwksControllerTest : CryptoTraderTest() {

    @Mock lateinit var rsaKeyService: RsaKeyService

    private lateinit var controller: JwksController

    @BeforeEach
    fun setUp() {
        controller = JwksController(rsaKeyService)
    }

    @Nested
    @Tag("jwks")
    @DisplayName("JWKS")
    inner class Jwks {
        @Test
        @DisplayName("Should return JWKS map with RSA key fields")
        fun jwks_ReturnsJwksMap() { }
    }
}
