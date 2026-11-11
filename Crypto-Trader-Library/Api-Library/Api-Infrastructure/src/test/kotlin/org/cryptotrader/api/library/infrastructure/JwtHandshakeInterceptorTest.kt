package org.cryptotrader.api.library.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.ProductUserService
import org.cryptotrader.api.library.services.jwt.JwtTokenService
import org.cryptotrader.api.library.services.jwt.TokenBlacklistService
import org.cryptotrader.api.library.services.rsa.RsaKeyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.http.HttpHeaders
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.socket.WebSocketHandler

@DisplayName("JwtHandshakeInterceptor")
@UnitSuite
class JwtHandshakeInterceptorTest : CryptoTraderTest() {

    private lateinit var jwtService: JwtTokenService
    private lateinit var productUserService: ProductUserService
    private lateinit var interceptor: JwtHandshakeInterceptor

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
        this.jwtService = JwtTokenService(rsa, "https://api.test", 300, "test-aud")
        this.productUserService = Mockito.mock(ProductUserService::class.java)
        val tokenBlacklistService = Mockito.mock(TokenBlacklistService::class.java)
        this.interceptor = JwtHandshakeInterceptor(jwtService, productUserService, tokenBlacklistService)
    }

    @Nested
    @DisplayName("beforeHandshake")
    inner class BeforeHandshake {
        @Nested
        @DisplayName("when Authorization bearer token is valid")
        inner class WhenAuthorizationBearerTokenIsValid {
            private var userId: Long = 0L
            private lateinit var email: String
            private lateinit var request: ServerHttpRequest

            private lateinit var response: ServerHttpResponse
            private lateinit var handler: WebSocketHandler
            private lateinit var attributes: MutableMap<String, Any>
            private lateinit var user: ProductUser

            @BeforeEach
            fun setUp() {
                userId = 10L
                email = "user@example.com"
                val token = jwtService.generateToken(userId.toString(), email)
                val headers = HttpHeaders()
                headers.add("Authorization", "Bearer $token")
                request = Mockito.mock(ServerHttpRequest::class.java)
                `when`(request.headers).thenReturn(headers)

                response = Mockito.mock(ServerHttpResponse::class.java)
                handler = Mockito.mock(WebSocketHandler::class.java)
                attributes = mutableMapOf<String, Any>()
                user = ProductUser.builder().email(email).build()
                user.id = userId
                `when`(productUserService.getUserById(userId)).thenReturn(user)
            }

            @Test
            @DisplayName("should attach user")
            fun attachUser() {
                val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

                assertThat(proceed).isTrue()
                assertThat(attributes["product-user"]).isEqualTo(user)
            }
        }

        @Nested
        @DisplayName("when query parameter token is valid")
        inner class WhenQueryParameterTokenIsValid {
            private var userId: Long = 0L
            private lateinit var email: String
            private lateinit var request: ServerHttpRequest

            private lateinit var response: ServerHttpResponse
            private lateinit var handler: WebSocketHandler
            private lateinit var attributes: MutableMap<String, Any>
            private lateinit var user: ProductUser

            @BeforeEach
            fun setUp() {
                userId = 20L
                email = "query@example.com"
                val token = jwtService.generateToken(userId.toString(), email)
                val servletRequest = MockHttpServletRequest()
                servletRequest.setParameter("token", token)
                request = ServletServerHttpRequest(servletRequest)

                response = Mockito.mock(ServerHttpResponse::class.java)
                handler = Mockito.mock(WebSocketHandler::class.java)
                attributes = mutableMapOf<String, Any>()
                user = ProductUser.builder().email(email).build()
                user.id = userId
                `when`(productUserService.getUserById(userId)).thenReturn(user)
            }

            @Test
            @DisplayName("should attach user")
            fun attachUser() {
                val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

                assertThat(proceed).isTrue()
                assertThat(attributes["product-user"]).isEqualTo(user)
            }
        }

        @Nested
        @DisplayName("when the token is invalid")
        inner class WhenTheTokenIsInvalid {
            private lateinit var request: ServerHttpRequest

            private lateinit var response: ServerHttpResponse
            private lateinit var handler: WebSocketHandler
            private lateinit var attributes: MutableMap<String, Any>

            @BeforeEach
            fun setUp() {
                val headers = HttpHeaders()
                headers.add("Authorization", "Bearer not-a-valid-token")
                request = Mockito.mock(ServerHttpRequest::class.java)
                `when`(request.headers).thenReturn(headers)

                response = Mockito.mock(ServerHttpResponse::class.java)
                handler = Mockito.mock(WebSocketHandler::class.java)
                attributes = mutableMapOf<String, Any>()
            }

            @Test
            @DisplayName("should ignore invalid token")
            fun ignoreInvalidToken() {
                val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

                assertThat(proceed).isTrue()
                assertThat(attributes).doesNotContainKey("product-user")
            }
        }

        @Nested
        @DisplayName("when email does not match")
        inner class WhenEmailDoesNotMatch {
            private var userId: Long = 0L
            private lateinit var tokenEmail: String

            private lateinit var request: ServerHttpRequest
            private lateinit var response: ServerHttpResponse
            private lateinit var handler: WebSocketHandler
            private lateinit var attributes: MutableMap<String, Any>
            private lateinit var user: ProductUser

            @BeforeEach
            fun setUp() {
                userId = 30L
                tokenEmail = "token@example.com"

                val actualEmail = "different@example.com"
                val token = jwtService.generateToken(userId.toString(), tokenEmail)
                val headers = HttpHeaders()
                headers.add("Authorization", "Bearer $token")
                request = Mockito.mock(ServerHttpRequest::class.java)
                `when`(request.headers).thenReturn(headers)
                response = Mockito.mock(ServerHttpResponse::class.java)
                handler = Mockito.mock(WebSocketHandler::class.java)
                attributes = mutableMapOf<String, Any>()
                user = ProductUser.builder().email(actualEmail).build()
                user.id = userId
                `when`(productUserService.getUserById(userId)).thenReturn(user)
            }

            @Test
            @DisplayName("should not attach user")
            fun notAttachUser() {
                val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

                assertThat(proceed).isTrue()
                assertThat(attributes).doesNotContainKey("product-user")
            }
        }

        @Nested
        @DisplayName("when no token is provided")
        inner class WhenNoTokenIsProvided {
            private lateinit var request: ServerHttpRequest

            private lateinit var response: ServerHttpResponse
            private lateinit var handler: WebSocketHandler
            private lateinit var attributes: MutableMap<String, Any>

            @BeforeEach
            fun setUp() {
                request = Mockito.mock(ServerHttpRequest::class.java)
                `when`(request.headers).thenReturn(HttpHeaders())

                response = Mockito.mock(ServerHttpResponse::class.java)
                handler = Mockito.mock(WebSocketHandler::class.java)
                attributes = mutableMapOf<String, Any>()
            }

            @Test
            @DisplayName("should proceed without attaching user")
            fun proceedWithoutAttachingUser() {
                val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

                assertThat(proceed).isTrue()
                assertThat(attributes).doesNotContainKey("product-user")
            }
        }
    }
}
