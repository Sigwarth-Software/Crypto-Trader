package org.cryptotrader.agent.library.component

import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.development.library.model.DevelopmentIssue
import org.cryptotrader.development.library.model.UserStoryIssue
import org.cryptotrader.development.library.services.GitHubIssueService
import org.cryptotrader.security.library.infrastructure.annotation.UserRestricted
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springframework.ai.tool.annotation.Tool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GitHubIssueTool @Autowired constructor(
    private val properties: AgentConstraintsProperties,
    private val gitHubIssueService: GitHubIssueService
) {
    companion object {
        private val SENSITIVE_CONTENT_PATTERNS: List<Regex> = listOf(
            Regex("(?i)api[_-]?key\\s*[:=]\\s*\\S+"),
            Regex("(?i)secret\\s*[:=]\\s*\\S+"),
            Regex("(?i)password\\s*[:=]\\s*\\S+"),
            Regex("(?i)access[_-]?token\\s*[:=]\\s*\\S+"),
            Regex("-----BEGIN [A-Z ]*PRIVATE KEY-----")
        )
        private val TASK_LINE_PATTERN: Regex = Regex("^-\\s*\\[[ xX]]\\s*(.+)$")
        private val POINTS_LINE_PATTERN: Regex = Regex("(?i)points\\s*:\\s*(\\d+)")
        private val LABELS_LINE_PATTERN: Regex = Regex("(?i)labels\\s*:\\s*(.+)")
        private val TITLE_LINE_PATTERN: Regex = Regex("(?i)^(?:title|issue)\\s*:\\s*(.+)$")
    }

    private val proposedIssues: MutableMap<UUID, DevelopmentIssue> = mutableMapOf()
    private var confirmedIssueId: UUID? = null

    @Tool(description = "Get the guidance prompt to follow when drafting a GitHub issue from a task or story " +
        "description, including the issue template and the repository's available labels. Call this before " +
        "createIssue so the drafted ticket text matches the expected format.")
    @UserRestricted
    fun getIssueCreationPrompt(
        @McpToolParam(description = "The task or story description to convert into a GitHub issue")
        prompt: String
    ): String {
        require(prompt.isNotBlank()) { "Prompt must not be blank." }

        return this.gitHubIssueService.getIssueCreationPrompt(prompt)
    }

    @Tool(description = "Propose a new GitHub issue from a user story written following the issue template " +
        "(title line, task checklist, and an hours-based points estimate). Returns the proposed issue for " +
        "review along with an issue ID to pass to confirmIssue.")
    @UserRestricted
    fun createIssue(
        @McpToolParam(description = "The drafted user story ticket text, following the issue template format")
        ticketRequest: String
    ): String {
        require(ticketRequest.isNotBlank()) { "Ticket request must not be blank." }
        this.requireNoSensitiveContent(ticketRequest)

        val issue: UserStoryIssue = this.parseTicketRequest(ticketRequest)
        val issueId: UUID = this.gitHubIssueService.createIssueUuid()
        this.proposedIssues[issueId] = issue

        return "${this.gitHubIssueService.presentIssue(issue)}\n\nIssue ID: $issueId"
    }

    @Tool(description = "Confirm a previously proposed issue by its ID, marking it ready to be published.")
    @UserRestricted
    fun confirmIssue(
        @McpToolParam(description = "The issue ID returned by createIssue")
        issueUuId: String
    ): String {
        val issueId: UUID = runCatching { UUID.fromString(issueUuId) }
            .getOrElse { throw IllegalArgumentException("Invalid issue ID: $issueUuId") }

        val issue: DevelopmentIssue = this.proposedIssues[issueId]
            ?: throw IllegalArgumentException("No proposed issue found for ID: $issueUuId")

        this.confirmedIssueId = issueId

        return "Issue confirmed and ready to publish.\n\n${this.gitHubIssueService.presentIssue(issue)}"
    }

    @Tool(description = "Publish the most recently confirmed issue to GitHub.")
    @UserRestricted
    fun publishIssue(): String {
        val issueId: UUID = this.confirmedIssueId
            ?: throw IllegalStateException("No issue has been confirmed. Call createIssue then confirmIssue first.")
        val issue: DevelopmentIssue = this.proposedIssues[issueId]
            ?: throw IllegalStateException("Confirmed issue could not be found: $issueId")

        val issueUrl: String = this.gitHubIssueService.publishIssue(issue)

        this.proposedIssues.remove(issueId)
        this.confirmedIssueId = null

        return "Issue published successfully: $issueUrl"
    }

    internal fun parseTicketRequest(ticketRequest: String): UserStoryIssue {
        val lines: List<String> = ticketRequest.lines().map { it.trim() }

        val tasks: List<String> = lines.mapNotNull { line -> TASK_LINE_PATTERN.find(line)?.groupValues?.get(1) }
        require(tasks.isNotEmpty()) { "Ticket request must contain at least one task checklist item." }

        val titleLine: String = lines.firstOrNull { line ->
            line.isNotBlank() && !TASK_LINE_PATTERN.matches(line) &&
                !POINTS_LINE_PATTERN.containsMatchIn(line) && !LABELS_LINE_PATTERN.containsMatchIn(line)
        } ?: throw IllegalArgumentException("Ticket request must contain a user story title line.")

        val title: String = TITLE_LINE_PATTERN.find(titleLine)?.groupValues?.get(1)?.trim() ?: titleLine

        val points: Int = lines.firstNotNullOfOrNull { line -> POINTS_LINE_PATTERN.find(line) }
            ?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val labels: List<String> = lines.firstNotNullOfOrNull { line -> LABELS_LINE_PATTERN.find(line) }
            ?.groupValues?.get(1)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        return UserStoryIssue(
            issueTitle = title,
            tasks = tasks,
            labels = labels,
            points = points
        )
    }

    internal fun requireNoSensitiveContent(ticketRequest: String) {
        val matchedPattern: Regex? = SENSITIVE_CONTENT_PATTERNS.firstOrNull { pattern -> pattern.containsMatchIn(ticketRequest) }

        if (matchedPattern != null) {
            throw SecurityException("Ticket request appears to contain sensitive data and cannot be processed.")
        }
    }
}
