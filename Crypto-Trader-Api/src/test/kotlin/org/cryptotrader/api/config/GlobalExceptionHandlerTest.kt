package org.cryptotrader.api.config

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("GlobalExceptionHandler")
@Tag("config")
@DisplayName("Global Exception Handler")
class GlobalExceptionHandlerTest : CryptoTraderTest() {

    private lateinit var handler: GlobalExceptionHandler

    @BeforeEach
    fun setUp() {
        handler = GlobalExceptionHandler()
    }

    @Nested
    @Tag("handleAnyException")
    @DisplayName("Handle Any Exception")
    inner class HandleAnyException {
        @Test
        @DisplayName("Should handle generic exceptions and log error")
        fun handleAnyException_LogsError() { }
    }
}
