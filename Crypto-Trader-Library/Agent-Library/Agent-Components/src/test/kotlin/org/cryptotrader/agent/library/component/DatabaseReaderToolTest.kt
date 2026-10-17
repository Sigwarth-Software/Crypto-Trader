package org.cryptotrader.agent.library.component

import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import javax.sql.DataSource

@Tag("DatabaseReaderTool")
@DisplayName("DatabaseReaderTool")
@UnitSuite
class DatabaseReaderToolTest : CryptoTraderTest() {
    @Mock
    lateinit var dataSource: DataSource

    @Mock
    lateinit var properties: AgentConstraintsProperties

    @InjectMocks
    lateinit var databaseReaderTool: DatabaseReaderTool

    @BeforeEach
    fun setUp() {

    }

    @Tag("listAllowedTables")
    @DisplayName("listAllowedTables")
    @Nested
    inner class ListAllowedTables {

        @Nested
        @DisplayName("when properties configured")
        inner class WhenPropertiesConfigured {
            @BeforeEach
            fun setUp() {
                `when`(properties.allowedTables).thenReturn(setOf("table1", "table2"))
            }

            @Test
            @DisplayName("should list configured tables")
            fun listConfiguredTables() {
                val tables: List<String> = databaseReaderTool.listAllowedTables()
                assertEquals(listOf("table1", "table2"), tables)
            }
        }
    }
}