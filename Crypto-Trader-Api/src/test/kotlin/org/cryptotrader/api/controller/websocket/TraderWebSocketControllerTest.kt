package org.cryptotrader.api.controller.websocket

import jakarta.websocket.Session
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mock

@Tag("TraderWebSocketController")
@Tag("websocket")
@DisplayName("Trader WebSocket Controller")
class TraderWebSocketControllerTest : CryptoTraderTest() {

    @Mock lateinit var session: Session

    private lateinit var controller: TraderWebSocketController

    @BeforeEach
    fun setUp() {
        controller = TraderWebSocketController()
    }

    @Nested
    @Tag("onOpen")
    @DisplayName("On Open")
    inner class OnOpen {
        @Test
        @DisplayName("Should handle new WebSocket session")
        fun onOpen_HandlesSession() { }
    }
}
