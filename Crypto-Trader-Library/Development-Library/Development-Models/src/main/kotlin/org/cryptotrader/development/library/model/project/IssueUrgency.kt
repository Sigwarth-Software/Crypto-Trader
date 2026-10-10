package org.cryptotrader.development.library.model.project

enum class IssueUrgency(val urgencyLevel: String) {
    HOTFIX("Hotfix"),
    URGENT("Urgent"),
    PRIORITY("Priority"),
    BACKLOG("Backlog");

    companion object {
        fun from(urgencyLevel: String): IssueUrgency? {
            return entries.find { it.urgencyLevel.equals(urgencyLevel, ignoreCase = true) }
        }
    }
}