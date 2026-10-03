package org.cryptotrader.development.library.model

class UserStoryIssue(
    issueTitle: String,
    tasks: List<String>,
    labels: List<String>,
    points: Int,
    extraContent: String? = null
) : DevelopmentIssue(
    issueTitle,
    tasksToDescription(tasks),
    labels,
    points,
    extraContent
) {
    companion object {
        fun tasksToDescription(tasks: List<String>): String {
            return tasks.joinToString(separator = "\n") { "- [ ] $it" }
        }
    }
}