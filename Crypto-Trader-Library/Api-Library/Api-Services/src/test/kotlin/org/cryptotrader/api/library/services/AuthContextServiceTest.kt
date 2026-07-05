package org.cryptotrader.api.library.services

import org.cryptotrader.api.library.services.jwt.JwtTokenService
import org.cryptotrader.api.library.services.jwt.TokenBlacklistService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder

@Tag("AuthContextService")
@Tag("service")
@DisplayName("Authentication Context Service")
class AuthContextServiceTest : CryptoTraderTest() {
    @Mock
    lateinit var jwtTokenService: JwtTokenService

    @Mock
    lateinit var tokenBlacklistService: TokenBlacklistService

    @Mock
    lateinit var productUserService: ProductUserService

    @Mock
    lateinit var authContext: Authentication

    @InjectMocks
    lateinit var authContextService: AuthContextService

    private val testEmail = "ollie@ollie.com"
    private val testUser = "ROLE_USER"
    private val testToken = "token"

    @BeforeEach
    fun setUp() {
        this.authContextService = AuthContextService(jwtTokenService, tokenBlacklistService, productUserService)
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
        RequestContextHolder.resetRequestAttributes()
    }

    @Nested
    @Tag("isAuthenticated")
    @DisplayName("Is Authenticated")
    inner class IsAuthenticated {
        @Test
        @DisplayName("Should return true when auth context is populated")
        fun isAuthenticated_ReturnsTrue_WhenPopulated() {
            val authentication = UsernamePasswordAuthenticationToken(testEmail,
                testToken,
                listOf(SimpleGrantedAuthority(testUser)))
            SecurityContextHolder.getContext().authentication = authentication

            val actualIsAuthenticated: Boolean = authContextService.isAuthenticated()
            assertTrue(actualIsAuthenticated)
        }

        @Test
        @DisplayName("Should return false when auth context is not populated")
        fun isAuthenticated_ReturnsFalse_WhenNotPopulated() {
            SecurityContextHolder.getContext().authentication = null
            val actualIsAuthenticated: Boolean = authContextService.isAuthenticated()
            assertFalse(actualIsAuthenticated)
        }
    }

    @Nested
    @Tag("logout")
    @DisplayName("Logout")
    inner class Logout {
        @Test
        @DisplayName("Should clear populated auth context")
        fun logout_ClearsAuthContext_WhenPopulated() {
            val authentication = UsernamePasswordAuthenticationToken(testEmail,
                testToken,
                listOf(SimpleGrantedAuthority(testUser)))
            SecurityContextHolder.getContext().authentication = authentication

            assertNotNull(SecurityContextHolder.getContext().authentication)
            authContextService.logout()
            assertNull(SecurityContextHolder.getContext().authentication)
        }
    }
}