package org.cryptotrader.api.library.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlin.jvm.java
import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.ProductUserService
import org.cryptotrader.security.library.event.UserIpDetectionEvent
import org.cryptotrader.security.library.event.publisher.SecurityEventsPublisher
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.*
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

@DisplayName("ClientIpFilter")
@UnitSuite
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
    @DisplayName("doFilter")
    inner class FilterInternal {

        @Nested
        @DisplayName("when user is authenticated")
        inner class WhenUserIsAuthenticated {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain
            private lateinit var testIp: String
            private lateinit var user: ProductUser

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                testIp = "192.168.1.100"
                `when`(request.remoteAddr).thenReturn(testIp)
                user = ProductUser.builder().email("trader@cryptotrader.org").build()
                user.id = 42L
                val authToken = TestingAuthenticationToken(user, null)
                SecurityContextHolder.getContext().authentication = authToken
            }

            @Test
            @DisplayName("should publish UserIpDetectionEvent")
            fun publishUserIpDetectionEvent() {
                filter.doFilter(request, response, chain)

                val captor = ArgumentCaptor.forClass(UserIpDetectionEvent::class.java)
                verify(securityEventsPublisher, times(1)).publish(captor.capture())
                assertThat(captor.value.ip).isEqualTo(testIp)
                assertThat(captor.value.productUser).isEqualTo(user)

                verify(chain, times(1)).doFilter(request, response)
            }
        }

        @Nested
        @DisplayName("when authentication is null")
        inner class WhenAuthenticationIsNull {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                `when`(request.remoteAddr).thenReturn("192.168.1.100")
                SecurityContextHolder.clearContext()
            }

            @Test
            @DisplayName("should not publish event")
            fun notPublishEvent() {
                filter.doFilter(request, response, chain)

                verify(securityEventsPublisher, never()).publish(any(UserIpDetectionEvent::class.java))
                verify(chain, times(1)).doFilter(request, response)
            }
        }

        @Nested
        @DisplayName("when principal is not a ProductUser")
        inner class WhenPrincipalIsNotAProductUser {
            private lateinit var request: HttpServletRequest

            private lateinit var response: HttpServletResponse
            private lateinit var chain: FilterChain

            @BeforeEach
            fun setUp() {
                request = mock(HttpServletRequest::class.java)

                response = mock(HttpServletResponse::class.java)
                chain = mock(FilterChain::class.java)
                `when`(request.remoteAddr).thenReturn("192.168.1.100")
                val anonymousToken = AnonymousAuthenticationToken(
                    "key",
                    "anonymousUser",
                    AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
                )
                SecurityContextHolder.getContext().authentication = anonymousToken
            }

            @Test
            @DisplayName("should not publish event")
            fun notPublishEvent() {
                filter.doFilter(request, response, chain)

                verify(securityEventsPublisher, never()).publish(any(UserIpDetectionEvent::class.java))
                verify(chain, times(1)).doFilter(request, response)
            }
        }
    }
}
