package org.cryptotrader.development.library.model.project

enum class ProjectIssuePriority(val priorityName: String) {
    HOTFIX("Hotfix"),
    URGENT("Urgent"),
    PRIORITY("Priority"),
    BACKLOG("Backlog");

    companion object {
        fun from(priorityName: String): ProjectIssuePriority? {
            return entries.find { it.priorityName.equals(priorityName, ignoreCase = true) }
        }
    }
}