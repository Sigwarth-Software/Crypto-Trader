package org.cryptotrader.api.library.services.jwt

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("TokenBlacklistService")
@UnitSuite
class TokenBlacklistServiceTest : CryptoTraderTest() {

    private lateinit var tokenBlacklistService: TokenBlacklistService

    @BeforeEach
    fun setUp() {
        this.tokenBlacklistService = TokenBlacklistService()
    }

    @Nested
    @DisplayName("blacklistToken")
    inner class BlacklistToken {
        @Test
        @DisplayName("should add a valid token to blacklist until expiry")
        fun addAValidTokenToBlacklistUntilExpiry() {
            assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
            val expiry = System.currentTimeMillis() + 100L
            tokenBlacklistService.blacklistToken("token", expiry)
            assertTrue(tokenBlacklistService.blacklistedTokens.isNotEmpty())
            Thread.sleep(100L)
            tokenBlacklistService.isBlacklisted("token")
            assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
        }

        @Nested
        @DisplayName("when the token is blank")
        inner class WhenTokenIsBlank {
            @Test
            @DisplayName("should leave the blacklist empty")
            fun leaveBlacklistEmpty() {
                assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
                val expiry = System.currentTimeMillis() + 5000L
                tokenBlacklistService.blacklistToken("", expiry)
                assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
            }
        }

        @Nested
        @DisplayName("when the token is already expired")
        inner class WhenTokenIsAlreadyExpired {
            @Test
            @DisplayName("should leave the blacklist empty")
            fun leaveBlacklistEmpty() {
                val expiry = System.currentTimeMillis() + 5000L
                tokenBlacklistService.blacklistToken("token", expiry - 100000L)
                assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
            }
        }
    }

    @Nested
    @DisplayName("isBlacklisted")
    inner class IsBlacklisted {
        @Nested
        @DisplayName("when token is blacklisted and not expired")
        inner class WhenTokenIsBlacklistedAndNotExpired {
            @BeforeEach
            fun setUp() {
                val expiry = System.currentTimeMillis() + 100_000L
                tokenBlacklistService.blacklistToken("token", expiry)
            }

            @Test
            @DisplayName("should return true")
            fun returnTrue() {
                assertTrue(tokenBlacklistService.isBlacklisted("token"))
            }
        }

        @Nested
        @DisplayName("when the token is not blacklisted")
        inner class WhenTokenIsNotBlacklisted {
            @Test
            @DisplayName("should return false")
            fun returnFalse() {
                assertFalse(tokenBlacklistService.isBlacklisted("token"))
            }
        }

        @Nested
        @DisplayName("when a blacklisted token expires")
        inner class WhenBlacklistedTokenExpires {
            @Test
            @DisplayName("should remove the token and return false")
            fun removeTokenAndReturnFalse() {
                val expiry = System.currentTimeMillis() + 100L
                tokenBlacklistService.blacklistToken("token", expiry)
                Thread.sleep(100L)
                tokenBlacklistService.isBlacklisted("token")
                assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
                assertFalse(tokenBlacklistService.isBlacklisted("token"))
            }
        }
    }
}
