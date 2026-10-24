package org.cryptotrader.api.library.services

import org.cryptotrader.api.library.services.jwt.JwtTokenService
import org.cryptotrader.api.library.services.jwt.TokenBlacklistService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder

@DisplayName("AuthContextService")
@UnitSuite
class AuthContextServiceTest : CryptoTraderTest() {
    @Mock
    lateinit var jwtTokenService: JwtTokenService

    @Mock
    lateinit var tokenBlacklistService: TokenBlacklistService

    @Mock
    lateinit var productUserService: ProductUserService

    @Mock
    lateinit var authContext: Authentication

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
    @DisplayName("isAuthenticated")
    inner class IsAuthenticated {
        @Nested
        @DisplayName("when auth context is populated")
        inner class WhenAuthContextIsPopulated {
            @BeforeEach
            fun setUp() {
                val authentication = UsernamePasswordAuthenticationToken(testEmail,
                    testToken,
                    listOf(SimpleGrantedAuthority(testUser)))
                SecurityContextHolder.getContext().authentication = authentication
            }

            @Test
            @DisplayName("should return true")
            fun returnTrue() {
                val actualIsAuthenticated: Boolean = authContextService.isAuthenticated()
                assertTrue(actualIsAuthenticated)
            }
        }

        @Nested
        @DisplayName("when auth context is not populated")
        inner class WhenAuthContextIsNotPopulated {
            @BeforeEach
            fun setUp() {
                SecurityContextHolder.getContext().authentication = null
            }

            @Test
            @DisplayName("should return false")
            fun returnFalse() {
                val actualIsAuthenticated: Boolean = authContextService.isAuthenticated()
                assertFalse(actualIsAuthenticated)
            }
        }
    }

    @Nested
    @DisplayName("logout")
    inner class Logout {
        @Nested
        @DisplayName("when the authentication context is populated")
        inner class WhenPopulated {
            private lateinit var authentication: UsernamePasswordAuthenticationToken

            @BeforeEach
            fun setUp() {
                authentication = UsernamePasswordAuthenticationToken(testEmail,
                    testToken,
                    listOf(SimpleGrantedAuthority(testUser)))
                SecurityContextHolder.getContext().authentication = authentication
            }

            @Test
            @DisplayName("should clear populated auth context")
            fun clearPopulatedAuthContext() {
                assertNotNull(SecurityContextHolder.getContext().authentication)
                authContextService.logout()
                assertNull(SecurityContextHolder.getContext().authentication)
            }
        }
    }
}
