package org.cryptotrader.development.library.model.project

enum class IssueType(val typeName: String) {
    TASK("Task"),
    BUG("Bug"),
    FEATURE("Feature");

    companion object {
        fun from(typeName: String): IssueType? {
            return entries.find { it.typeName.equals(typeName, ignoreCase = true) }
        }
    }
}