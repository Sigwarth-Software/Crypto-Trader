package org.cryptotrader.universal.library.component

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext

@Tag("SpringContext")
@Tag("component")
@DisplayName("Spring Context Helper")
class SpringContextTest : CryptoTraderTest() {

    lateinit var springContext: SpringContext

    @BeforeEach
    fun setUp() {
        this.springContext = SpringContext()
    }

    @Nested
    @Tag("setApplicationContext")
    @DisplayName("Set Application Context")
    inner class SetApplicationContext {
        @Test
        @DisplayName("Should set static application context")
        fun setApplicationContext_SetsStaticContext() {
            val appContext = mock(ApplicationContext::class.java)
            springContext.setApplicationContext(appContext)
            assertEquals(appContext, SpringContext.getContext())
        }
    }

    @Nested
    @Tag("getBean")
    @DisplayName("Get Bean")
    inner class GetBean {
        @Test
        @DisplayName("Should retrieve bean from static context")
        fun getBean_Retrieves() {
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