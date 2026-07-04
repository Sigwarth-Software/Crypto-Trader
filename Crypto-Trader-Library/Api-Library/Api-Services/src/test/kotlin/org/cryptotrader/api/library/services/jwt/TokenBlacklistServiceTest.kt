package org.cryptotrader.api.library.services.jwt

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.springframework.test.util.AssertionErrors.assertFalse
import java.time.LocalDateTime
import java.util.Date
import kotlin.time.Instant

@Tag("TokenBlacklistService")
@Tag("service")
@DisplayName("Token Blacklist Service")
class TokenBlacklistServiceTest : CryptoTraderTest() {

    @InjectMocks
    private lateinit var tokenBlacklistService: TokenBlacklistService

    @BeforeEach
    fun setUp() {
        this.tokenBlacklistService = TokenBlacklistService()
    }

    @Nested
    @Tag("blacklistToken")
    @DisplayName("Blacklist Token")
    inner class BlacklistToken {
        @Test
        @DisplayName("Should add a valid token to blacklist until expiry")
        fun blacklistToken_BlacklistsUntilExpiry_ValidToken() {
            assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
            val expiry = System.currentTimeMillis() + 100L
            tokenBlacklistService.blacklistToken("token", expiry)
            assertTrue(tokenBlacklistService.blacklistedTokens.isNotEmpty())
            Thread.sleep(100L)
            tokenBlacklistService.isBlacklisted("token")
            assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
        }

        @Test
        @DisplayName("Should ignore blank or expired tokens")
        fun blacklistToken_Ignores_BlankOrExpiredTokens() {
            assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
            val expiry = System.currentTimeMillis() + 5000L
            tokenBlacklistService.blacklistToken("", expiry)
            assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
            tokenBlacklistService.blacklistToken("token", expiry - 100000L)
            assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
        }
    }

    @Nested
    @Tag("isBlacklisted")
    @DisplayName("Is Blacklisted")
    inner class IsBlacklisted {
        @Test
        @DisplayName("Should return true when token is blacklisted and not expired")
        fun isBlacklisted_ReturnsTrue_WhenActive() {
            val expiry = System.currentTimeMillis() + 100_000L;
            tokenBlacklistService.blacklistToken("token", expiry)
            assertTrue(tokenBlacklistService.isBlacklisted("token"))
        }

        @Test
        @DisplayName("Should return false when token is not blacklisted or expired")
        fun isBlacklisted_ReturnsFalse_WhenNotActive() {
            assertFalse(tokenBlacklistService.isBlacklisted("token"))
            val expiry = System.currentTimeMillis() + 100L
            tokenBlacklistService.blacklistToken("token", expiry)
            Thread.sleep(100L)
            tokenBlacklistService.isBlacklisted("token")
            assertTrue(tokenBlacklistService.blacklistedTokens.isEmpty())
            assertFalse(tokenBlacklistService.isBlacklisted("token"))
        }
    }
}