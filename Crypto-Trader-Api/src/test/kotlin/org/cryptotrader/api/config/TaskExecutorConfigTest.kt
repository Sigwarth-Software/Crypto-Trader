package org.cryptotrader.api.config

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("TaskExecutorConfig")
@Tag("config")
@DisplayName("Task Executor Configuration")
class TaskExecutorConfigTest : CryptoTraderTest() {

    private lateinit var config: TaskExecutorConfig

    @BeforeEach
    fun setUp() {
        config = TaskExecutorConfig()
    }

    @Nested
    @Tag("threadPoolTaskExecutor")
    @DisplayName("ThreadPoolTaskExecutor Bean")
    inner class ThreadPoolTaskExecutorBean {
        @Test
        @DisplayName("Should provide TaskExecutor bean")
        fun threadPoolTaskExecutor_ProvidesBean() { }
    }
}
