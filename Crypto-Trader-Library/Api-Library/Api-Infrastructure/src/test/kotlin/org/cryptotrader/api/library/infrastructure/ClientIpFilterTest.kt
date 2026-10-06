package org.cryptotrader.api.library.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.ProductUserService
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.security.library.event.publisher.SecurityEventsPublisher
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.*
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.jvm.java

@Tag("infrastructure")
@Tag("ClientIpFilter")
@DisplayName("Client IP Filter")
class ClientIpFilterTest : CryptoTraderTest() {
    private lateinit var securityEventsPublisher: SecurityEventsPublisher
    private lateinit var filter: ClientIpFilter
    private lateinit var userService: ProductUserService

    @BeforeEach
    fun setUp() {
        this.securityEventsPublisher = mock(SecurityEventsPublisher::class.java)
        this.userService = mock(ProductUserService::class.java)
        this.filter = ClientIpFilter(this.securityEventsPublisher, this.userService)
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Nested
    @DisplayName("Filter Internal")
    inner class FilterInternal {

        @Test
        @DisplayName("Should publish UserIpDetectionEvent when user is authenticated")
        fun doFilterInternal_PublishesEvent_WhenUserIsAuthenticated() {
            val request = mock(HttpServletRequest::class.java)
            val response = mock(HttpServletResponse::class.java)
            val chain = mock(FilterChain::class.java)

            val testIp = "192.168.1.100"
            `when`(request.remoteAddr).thenReturn(testIp)

            val user = ProductUser.builder().email("trader@cryptotrader.org").build()
            user.id = 42L

            val authToken = TestingAuthenticationToken(user, null)
            SecurityContextHolder.getContext().authentication = authToken

            filter.doFilter(request, response, chain)

            val captor = ArgumentCaptor.forClass(UserIpDetectionEvent::class.java)
            verify(securityEventsPublisher, times(1)).publish(captor.capture())
            assertThat(captor.value.ip).isEqualTo(testIp)
            assertThat(captor.value.productUser).isEqualTo(user)

            verify(chain, times(1)).doFilter(request, response)
        }

        @Test
        @DisplayName("Should not publish event when authentication is null")
        fun doFilterInternal_DoesNotPublish_WhenAuthenticationIsNull() {
            val request = mock(HttpServletRequest::class.java)
            val response = mock(HttpServletResponse::class.java)
            val chain = mock(FilterChain::class.java)

            `when`(request.remoteAddr).thenReturn("192.168.1.100")
            SecurityContextHolder.clearContext()

            filter.doFilter(request, response, chain)

            verify(securityEventsPublisher, never()).publish(any(UserIpDetectionEvent::class.java))
            verify(chain, times(1)).doFilter(request, response)
        }

        @Test
        @DisplayName("Should not publish event when principal is not a ProductUser")
        fun doFilterInternal_DoesNotPublish_WhenPrincipalIsNotProductUser() {
            val request = mock(HttpServletRequest::class.java)
            val response = mock(HttpServletResponse::class.java)
            val chain = mock(FilterChain::class.java)

            `when`(request.remoteAddr).thenReturn("192.168.1.100")

            val anonymousToken = AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
            )
            SecurityContextHolder.getContext().authentication = anonymousToken

            filter.doFilter(request, response, chain)

            verify(securityEventsPublisher, never()).publish(any(UserIpDetectionEvent::class.java))
            verify(chain, times(1)).doFilter(request, response)
        }
    }
}
