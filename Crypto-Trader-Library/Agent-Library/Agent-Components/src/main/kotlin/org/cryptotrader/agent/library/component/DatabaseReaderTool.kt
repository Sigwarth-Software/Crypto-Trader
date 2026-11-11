package org.cryptotrader.agent.library.component

import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.agent.library.model.database.DatabaseColumnMetadata
import org.cryptotrader.agent.library.model.database.DatabaseTableValidationMessage
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.DatabaseMetaData.*
import java.sql.ResultSet
import javax.sql.DataSource

@Component
class DatabaseReaderTool(
    private val dataSource: DataSource,
    private val properties: AgentConstraintsProperties
) {

    @Tool(description = "List allowlisted tables the agent may inspect.")
    fun listAllowedTables(): List<String> {
        return this.properties.allowedTables.sorted()
    }

    @Tool(description = "Describes the columns and schema of a given table" +
        " in \"schema.table\" format. The table name is case-insensitive " +
        "and must be from the allowlisted tables.")
    fun describeTable(
        @McpToolParam(description = "Qualified table name in schema.table format (e.g., public.trades)")
        qualifiedTableName: String
    ): List<DatabaseColumnMetadata> {
        val tableValidityMessage: DatabaseTableValidationMessage = this.isValidTable(qualifiedTableName)
        require(tableValidityMessage == DatabaseTableValidationMessage.SUCCESS) { tableValidityMessage.name }
        val (schema: String, table: String) = qualifiedTableName.lowercase().split(".", limit = 2)
        this.dataSource.connection.use { connection ->
            val columns: List<DatabaseColumnMetadata> = this.fetchColumns(connection, schema, table)
            return columns
        }
    }

    private fun fetchColumns(
        connection: Connection,
        schema: String?,
        table: String
    ): List<DatabaseColumnMetadata> {
        val meta: DatabaseMetaData = connection.metaData
        // null is to be JDBC search-friendly for missing content.
        val catalogs: List<String?> = listOf(connection.catalog, null).distinct()

        catalogs.forEach { catalog: String? ->
            meta.getColumns(
                catalog,
                schema,
                table,
                "%"
            ).use { columnData: ResultSet ->
                val columns: MutableList<DatabaseColumnMetadata> = mutableListOf()
                while (columnData.next()) {
                    columns.add(
                        DatabaseColumnMetadata(
                            name = columnData.getString("COLUMN_NAME"),
                            type = columnData.getString("TYPE_NAME"),
                            nullable = columnData.getInt("NULLABLE") != columnNoNulls
                        )
                    )
                }
                if (columns.isNotEmpty()) {
                    return columns
                }
            }
        }
        return emptyList()
    }


    internal fun isValidTable(qualifiedTableName: String): DatabaseTableValidationMessage {
        if (qualifiedTableName.count { it == '.' } != 1) {
            return DatabaseTableValidationMessage.INVALID_FORMATTING
        }
        if (qualifiedTableName.lowercase() !in this.properties.allowedTables) {
            return DatabaseTableValidationMessage.TABLE_NOT_ALLOWLISTED
        }
        return DatabaseTableValidationMessage.SUCCESS
    }
}
