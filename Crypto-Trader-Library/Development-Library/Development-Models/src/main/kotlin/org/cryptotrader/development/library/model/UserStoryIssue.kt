package org.cryptotrader.development.library.model

import org.cryptotrader.development.library.model.project.GitHubProjectModules
import org.cryptotrader.development.library.model.project.IssuePriority
import org.cryptotrader.development.library.model.project.IssueType
import org.cryptotrader.development.library.model.project.IssueUrgency

class UserStoryIssue(
    issueTitle: String,
    tasks: List<String>,
    labels: List<String>,
    points: Int,
    extraContent: String? = null,
    urgency: IssueUrgency? = null,
    modules: GitHubProjectModules? = null,
    priority: IssuePriority? = null,
    type: IssueType? = null
) : DevelopmentIssue(
    issueTitle,
    tasksToDescription(tasks),
    labels,
    points,
    extraContent,
    urgency,
    modules,
    priority,
    type
) {
    companion object {
        fun tasksToDescription(tasks: List<String>): String {
            return tasks.joinToString(separator = "\n") { "- [ ] $it" }
        }
    }
}
