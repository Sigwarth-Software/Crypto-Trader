package org.cryptotrader.api.library.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.ProductUserService
import org.cryptotrader.api.library.services.jwt.JwtTokenService
import org.cryptotrader.api.library.services.jwt.TokenBlacklistService
import org.cryptotrader.api.library.services.rsa.RsaKeyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

@DisplayName("JwtAuthenticationFilter")
@UnitSuite
class JwtAuthenticationFilterTest : CryptoTraderTest() {
    private lateinit var jwtService: JwtTokenService
    private lateinit var productUserService: ProductUserService
    private lateinit var tokenBlacklistService: TokenBlacklistService
    private lateinit var filter: JwtAuthenticationFilter

    val testSecret: String = "secret"
    val testIssuer: String = "issuer"
    val testExpiration: Long = 3600
    val testPrincipal: String = "user"
    val testCredentials: String = "cred"
    val testUserId: Long = 10L
    val testEmail: String = "ollie@ollie.com"

    @BeforeEach
    fun setUp() {
        val rsa = RsaKeyService(null, null, null)
        this.jwtService = JwtTokenService(rsa, this.testIssuer, 300, "test-aud")
        this.productUserService = mock(ProductUserService::class.java)
        this.tokenBlacklistService = mock(TokenBlacklistService::class.java)
        this.filter = JwtAuthenticationFilter(this.jwtService, this.productUserService, this.tokenBlacklistService)
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Nested
    @DisplayName("doFilter")
    inner class Authentication {
        @Nested
        @DisplayName("when the authorization header is missing")
        inner class WhenTheAuthorizationHeaderIsMissing {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                `when`(request.getHeader("Authorization")).thenReturn(null)
                val testingAuthenticationToken = TestingAuthenticationToken(
                    testPrincipal,
                    testCredentials
                )
                SecurityContextHolder.getContext().authentication = testingAuthenticationToken
            }

            @Test
            @DisplayName("should clear the context and continue the chain")
            fun clearTheContextAndContinueTheChain() {
                filter.doFilter(request, response, chain)
                verify(chain, times(1)).doFilter(request, response)
                assertThat(SecurityContextHolder.getContext().authentication).isNull()
            }
        }

        @Nested
        @DisplayName("when the token is invalid")
        inner class WhenTheTokenIsInvalid {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                `when`(request.getHeader("Authorization")).thenReturn("Bearer not-a-valid-token")
                SecurityContextHolder.getContext().authentication =
                    TestingAuthenticationToken("user", "cred")
            }

            @Test
            @DisplayName("should clear the context and continue the chain")
            fun clearTheContextAndContinueTheChain() {
                filter.doFilter(request, response, chain)

                verify(chain, times(1)).doFilter(request, response)
                assertThat(SecurityContextHolder.getContext().authentication).isNull()
            }
        }

        @Nested
        @DisplayName("when a valid token matches the user")
        inner class WhenAValidTokenMatchesTheUser {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain
            private lateinit var user: ProductUser

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                val userId = 42L
                val email = "test@example.com"
                val token = jwtService.generateToken(userId.toString(), email)
                `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
                user = ProductUser.builder().email(email).build()
                user.id = userId
                `when`(productUserService.getUserById(userId)).thenReturn(user)
            }

            @Test
            @DisplayName("should authenticate the request")
            fun authenticateTheRequest() {
                filter.doFilter(request, response, chain)

                verify(chain, times(1)).doFilter(request, response)
                val auth = SecurityContextHolder.getContext().authentication
                assertThat(auth).isNotNull
                assertThat(auth!!.isAuthenticated).isTrue()
                assertThat(auth.principal).isEqualTo(user)
            }
        }

        @Nested
        @DisplayName("when a valid token has no matching user")
        inner class WhenAValidTokenHasNoMatchingUser {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                val userId = 99L
                val email = "someone@example.com"
                val token = jwtService.generateToken(userId.toString(), email)
                `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
                `when`(productUserService.getUserById(userId)).thenReturn(null)
            }

            @Test
            @DisplayName("should clear the context")
            fun clearTheContext() {
                filter.doFilter(request, response, chain)

                verify(chain, times(1)).doFilter(request, response)
                assertThat(SecurityContextHolder.getContext().authentication).isNull()
            }
        }

        @Nested
        @DisplayName("when the token email does not match the user")
        inner class WhenTheTokenEmailDoesNotMatchTheUser {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                val userId = 7L
                val tokenEmail = "token@example.com"
                val userEmail = "real@example.com"
                val token = jwtService.generateToken(userId.toString(), tokenEmail)
                `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
                val user = ProductUser.builder().email(userEmail).build()
                user.id = userId
                `when`(productUserService.getUserById(userId)).thenReturn(user)
            }

            @Test
            @DisplayName("should clear context on email mismatch")
            fun clearContextOnEmailMismatch() {
                filter.doFilter(request, response, chain)

                verify(chain, times(1)).doFilter(request, response)
                assertThat(SecurityContextHolder.getContext().authentication).isNull()
            }
        }

        @Nested
        @DisplayName("when the token is blacklisted")
        inner class WhenTheTokenIsBlacklisted {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                val userId = 123L
                val email = "blacklisted@example.com"
                val token = jwtService.generateToken(userId.toString(), email)
                `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
                `when`(tokenBlacklistService.isBlacklisted(token)).thenReturn(true)
                SecurityContextHolder.getContext().authentication =
                    TestingAuthenticationToken("user", "cred")
            }

            @Test
            @DisplayName("should clear context on blacklisted token")
            fun clearContextOnBlacklistedToken() {
                filter.doFilter(request, response, chain)

                verify(chain, times(1)).doFilter(request, response)
                assertThat(SecurityContextHolder.getContext().authentication).isNull()
            }
        }
    }
}
