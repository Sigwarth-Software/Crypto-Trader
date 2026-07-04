package org.cryptotrader.api.library.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.jwt.JwtTokenService
import org.cryptotrader.api.library.services.ProductUserService
import org.cryptotrader.api.library.services.jwt.TokenBlacklistService
import org.cryptotrader.api.library.services.rsa.RsaKeyService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.http.HttpHeaders
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.socket.WebSocketHandler

@Tag("infrastructure")
@Tag("JwtHandshakeInterceptor")
@DisplayName("JWT Handshake Interceptor")
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
    @DisplayName("Before Handshake")
    inner class BeforeHandshake {
        @Test
        @DisplayName("Should attach user when Authorization bearer token is valid")
        fun beforeHandshake_AttachesUser_ValidToken() {
            val userId = 10L
            val email = "user@example.com"
            val token = jwtService.generateToken(userId.toString(), email)

            val headers = HttpHeaders()
            headers.add("Authorization", "Bearer $token")
            val request = Mockito.mock(ServerHttpRequest::class.java)
            `when`(request.headers).thenReturn(headers)

            val response = Mockito.mock(ServerHttpResponse::class.java)
            val handler = Mockito.mock(WebSocketHandler::class.java)
            val attributes = mutableMapOf<String, Any>()

            val user = ProductUser.builder().email(email).build()
            user.id = userId
            `when`(productUserService.getUserById(userId)).thenReturn(user)

            val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

            assertThat(proceed).isTrue()
            assertThat(attributes["product-user"]).isEqualTo(user)
        }

        @Test
        @DisplayName("Should attach user when query parameter token is valid")
        fun beforeHandshake_AttachesUser_ValidQueryParameter() {
            val userId = 20L
            val email = "query@example.com"
            val token = jwtService.generateToken(userId.toString(), email)

            val servletRequest = MockHttpServletRequest()
            servletRequest.setParameter("token", token)
            val request: ServerHttpRequest = ServletServerHttpRequest(servletRequest)

            val response = Mockito.mock(ServerHttpResponse::class.java)
            val handler = Mockito.mock(WebSocketHandler::class.java)
            val attributes = mutableMapOf<String, Any>()

            val user = ProductUser.builder().email(email).build()
            user.id = userId
            `when`(productUserService.getUserById(userId)).thenReturn(user)

            val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

            assertThat(proceed).isTrue()
            assertThat(attributes["product-user"]).isEqualTo(user)
        }

        @Test
        @DisplayName("Should ignore invalid token")
        fun beforeHandshake_Ignores_InvalidToken() {
            val headers = HttpHeaders()
            headers.add("Authorization", "Bearer not-a-valid-token")
            val request = Mockito.mock(ServerHttpRequest::class.java)
            `when`(request.headers).thenReturn(headers)

            val response = Mockito.mock(ServerHttpResponse::class.java)
            val handler = Mockito.mock(WebSocketHandler::class.java)
            val attributes = mutableMapOf<String, Any>()

            val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

            assertThat(proceed).isTrue()
            assertThat(attributes).doesNotContainKey("product-user")
        }

        @Test
        @DisplayName("Should not attach user when email does not match")
        fun beforeHandshake_DoesNotAttachUser_EmailMismatch() {
            val userId = 30L
            val tokenEmail = "token@example.com"
            val actualEmail = "different@example.com"
            val token = jwtService.generateToken(userId.toString(), tokenEmail)

            val headers = HttpHeaders()
            headers.add("Authorization", "Bearer $token")
            val request = Mockito.mock(ServerHttpRequest::class.java)
            `when`(request.headers).thenReturn(headers)

            val response = Mockito.mock(ServerHttpResponse::class.java)
            val handler = Mockito.mock(WebSocketHandler::class.java)
            val attributes = mutableMapOf<String, Any>()

            val user = ProductUser.builder().email(actualEmail).build()
            user.id = userId
            `when`(productUserService.getUserById(userId)).thenReturn(user)

            val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

            assertThat(proceed).isTrue()
            assertThat(attributes).doesNotContainKey("product-user")
        }

        @Test
        @DisplayName("Should proceed without attaching user when no token")
        fun beforeHandshake_Proceeds_NoToken() {
            val request = Mockito.mock(ServerHttpRequest::class.java)
            `when`(request.headers).thenReturn(HttpHeaders())

            val response = Mockito.mock(ServerHttpResponse::class.java)
            val handler = Mockito.mock(WebSocketHandler::class.java)
            val attributes = mutableMapOf<String, Any>()

            val proceed = interceptor.beforeHandshake(request, response, handler, attributes)

            assertThat(proceed).isTrue()
            assertThat(attributes).doesNotContainKey("product-user")
        }
    }
}
