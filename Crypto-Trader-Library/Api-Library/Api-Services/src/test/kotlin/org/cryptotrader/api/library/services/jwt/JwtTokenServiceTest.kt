package org.cryptotrader.api.library.services.jwt

import org.cryptotrader.api.library.model.jwt.JwtClaims
import org.cryptotrader.api.library.services.rsa.RsaKeyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertNotNull

@DisplayName("JWT Token Service")
class JwtTokenServiceTest : CryptoTraderTest() {

    lateinit var jwtTokenService: JwtTokenService

    @BeforeEach
    fun setUp() {
        val rsa = RsaKeyService(null, null, null)
        this.jwtTokenService = JwtTokenService(
            rsa,
            "https://sscryptotrader.com",
            300,
            "crypto-trader-api"
        )
    }

    @Nested
    @DisplayName("Initialization")
    inner class Initialization {
        @Test
        @DisplayName("Should initialize service with valid values")
        fun unit_Initialize_ValidValues() {
            assertDoesNotThrow {
                val rsa = RsaKeyService(null, null, null)
                JwtTokenService(
                    rsa,
                    "https://api.cryptotrader.com",
                    300,
                    "crypto-trader-api"
                )
            }
        }
    }

    @Nested
    @DisplayName("Token Operations")
    inner class TokenOperations {
        @Test
        @DisplayName("Should generate a token with valid values and parse claims")
        fun generateAndParse_GeneratesAndParsesToken() {
            val token: String = jwtTokenService.generateToken("1", "ollie@ollie.com")
            assert(token.isNotBlank())
            val decodedClaims: JwtClaims = jwtTokenService.validateAndParse(token)
            assert(decodedClaims.subject == "1")
            assert(decodedClaims.email == "ollie@ollie.com")
            assertNotNull(decodedClaims.issuedAt)
            assertNotNull(decodedClaims.expiresAt)
        }
    }
}