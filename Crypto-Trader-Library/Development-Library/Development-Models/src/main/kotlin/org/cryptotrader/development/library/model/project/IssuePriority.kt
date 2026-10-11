package org.cryptotrader.development.library.model.project

enum class IssuePriority(val priorityName: String) {
    URGENT("Urgent"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    companion object {
        fun from(priorityName: String): IssuePriority? {
            return entries.find { it.priorityName.equals(priorityName, ignoreCase = true) }
        }
    }
}