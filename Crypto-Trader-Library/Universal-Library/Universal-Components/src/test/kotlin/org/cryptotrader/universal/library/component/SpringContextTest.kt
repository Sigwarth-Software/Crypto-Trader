package org.cryptotrader.universal.library.component

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.context.ApplicationContext

@DisplayName("SpringContext")
@UnitSuite
class SpringContextTest : CryptoTraderTest() {

    lateinit var springContext: SpringContext

    @BeforeEach
    fun setUp() {
        this.springContext = SpringContext()
    }

    @Nested
    @DisplayName("setApplicationContext")
    inner class SetApplicationContext {
        @Test
        @DisplayName("should set static application context")
        fun setStaticApplicationContext() {
            val appContext = mock(ApplicationContext::class.java)
            springContext.setApplicationContext(appContext)
            assertEquals(appContext, SpringContext.getContext())
        }
    }

    @Nested
    @DisplayName("getBean")
    inner class GetBean {
        @Test
        @DisplayName("should retrieve bean from static context")
        fun retrieveBeanFromStaticContext() {
            val bean = Any()
            val appContext = mock(ApplicationContext::class.java)
            springContext.setApplicationContext(appContext)
            `when`(
                SpringContext.getContext().getBean(Any::class.java)
            ).thenReturn(bean)
            assertEquals(
                bean,
                SpringContext.getBean(Any::class.java)
            )
        }
    }
}
