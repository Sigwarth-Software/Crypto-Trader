package org.cryptotrader.development.library.model.project

enum class IssueEffort(val effortName: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    companion object {
        fun from(effortName: String): IssueEffort? {
            return entries.find { it.effortName.equals(effortName, ignoreCase = true) }
        }
    }
}