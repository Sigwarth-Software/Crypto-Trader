package org.cryptotrader.agent.library.model.database

enum class DatabaseTableValidationMessage(message: String) {
    SUCCESS("No issues found."),
    INVALID_FORMATTING("Invalid format. Use schema.table format."),
    TABLE_NOT_ALLOWLISTED("Table not in allowedTables.")
}