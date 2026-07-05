package org.cryptotrader.agent.library.component

import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import javax.sql.DataSource
class DatabaseReaderToolTest : CryptoTraderTest() {
    @Mock
    lateinit var dataSource: DataSource
    @Mock
    lateinit var properties: AgentConstraintsProperties

    lateinit var databaseReaderTool: DatabaseReaderTool

    @BeforeEach
    fun setUp() {
        this.databaseReaderTool = DatabaseReaderTool(this.dataSource, this.properties)
    }

    @Nested
    inner class ListAllowedTables {
        @Test
        fun listsTables_WhenPropertiesConfigured() {
            `when`(properties.allowedTables).thenReturn(setOf("table1", "table2"))
            val tables = databaseReaderTool.listAllowedTables()
            assertEquals(tables, listOf("table1", "table2"))
        }
    }
}