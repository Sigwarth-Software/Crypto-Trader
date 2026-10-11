package org.cryptotrader.development.library.model

import org.cryptotrader.development.library.model.project.GitHubProjectModule
import org.cryptotrader.development.library.model.project.IssueEffort
import org.cryptotrader.development.library.model.project.IssuePriority
import org.cryptotrader.development.library.model.project.IssueSize
import org.cryptotrader.development.library.model.project.ProjectIssuePriority
import org.cryptotrader.development.library.model.project.IssueType

class UserStoryIssue(
    issueTitle: String,
    tasks: List<String>,
    labels: List<String>,
    points: Int,
    extraContent: String? = null,
    modules: GitHubProjectModule? = null,
    priority: IssuePriority? = null,
    projectPriority: ProjectIssuePriority? = null,
    effort: IssueEffort? = null,
    type: IssueType? = null,
    size: IssueSize? = null,
) : DevelopmentIssue(
    issueTitle,
    tasksToDescription(tasks),
    labels,
    points,
    extraContent,
    modules,
    priority,
    projectPriority,
    effort,
    type,
    size
) {
    companion object {
        fun tasksToDescription(tasks: List<String>): String {
            return tasks.joinToString(separator = "\n") { "- [ ] $it" }
        }
    }
}
