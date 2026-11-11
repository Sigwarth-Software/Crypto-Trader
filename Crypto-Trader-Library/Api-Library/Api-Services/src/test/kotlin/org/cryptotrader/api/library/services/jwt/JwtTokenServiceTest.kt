package org.cryptotrader.api.library.services.jwt

import org.cryptotrader.api.library.model.jwt.JwtClaims
import org.cryptotrader.api.library.services.rsa.RsaKeyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertNotNull

@DisplayName("JwtTokenService")
@UnitSuite
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
    @DisplayName("constructor")
    inner class Initialization {
        @Test
        @DisplayName("should initialize service with valid values")
        fun initializeServiceWithValidValues() {
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
    @DisplayName("generateToken and validateAndParse")
    inner class TokenOperations {
        @Test
        @DisplayName("should generate a token with valid values and parse claims")
        fun generateATokenWithValidValuesAndParseClaims() {
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
