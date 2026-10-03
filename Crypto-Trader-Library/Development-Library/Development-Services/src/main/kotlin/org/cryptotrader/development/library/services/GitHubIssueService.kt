package org.cryptotrader.development.library.services

import org.cryptotrader.development.library.model.DevelopmentIssue
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueBuilder
import org.kohsuke.github.GHRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.UUID
import kotlin.jvm.java

@Service
class GitHubIssueService @Autowired constructor(
    private val repository: GHRepository,
    private val githubMetadataService: GitHubMetadataService
) {
    companion object {
        const val ISSUE_TEMPLATE_LOCATION = "USER_STORY_ISSUE_TEMPLATE.md"
        val log: Logger = LoggerFactory.getLogger(GitHubIssueService::class.java)
    }

    fun <T : DevelopmentIssue> publishIssue(issue: T): String {
        val issueTitle: String = issue.issueTitle
        val issueDescription: String = issue.getDescription()
        val issueLabels: List<String> = this.validateLabels(issue.labels)

        val builder: GHIssueBuilder = this.repository.createIssue(issueTitle)

        builder.body(issueDescription)
        issueLabels.forEach { label -> builder.label(label) }

        val createdIssue: GHIssue = builder.create()
        log.info("Published issue #${createdIssue.number} at ${createdIssue.htmlUrl}")

        return createdIssue.htmlUrl.toString()
    }

    fun validateLabels(requestedLabels: List<String>): List<String> {
        val availableLabels: List<String> = this.githubMetadataService.getAvailableLabelsNames()

        val (validLabels: List<String>, invalidLabels: List<String>) = requestedLabels.partition {
            availableLabels.contains(it)
        }

        if (invalidLabels.isNotEmpty()) {
            log.warn("Dropping unknown labels not present in the repository: $invalidLabels")
        }

        return validLabels
    }

    fun <T : DevelopmentIssue> presentIssue(issue: T): String {
        val issueDescription: String = issue.getDescription()
        val prompt: String = "Does the following ticket properly represent the task or story you want to create? (Yes/No/Stop) (Y/N/S)"

        return "$prompt\n\n$issueDescription"
    }

    fun getIssueCreationPrompt(prompt: String): String {
        val availableLabels: List<String> = this.githubMetadataService.getAvailableLabelsNames()

        return """
            You are a software development assistant.
            Your task is to help create a new GitHub issue for a software project.

            You will be given a task or story, that you will convert into a GitHub issue.

            The user has provided this prompt for the issue creation:
                "$prompt"

            Your template for the issue is as follows:
                ${this.getIssueTemplate()}

            You're available tags include (use all that apply):
                ${availableLabels.joinToString(separator = "\n") { "- $it" }}

        """.trimIndent()
    }

    fun getIssueTemplate(): String {
        val currentClassLoader: ClassLoader = this::class.java.classLoader
        val templateStream: InputStream? = currentClassLoader.getResourceAsStream(
            ISSUE_TEMPLATE_LOCATION
        )

        if (templateStream == null) {
            log.error(
                "Issue template not found at location: $ISSUE_TEMPLATE_LOCATION"
            )
            throw IllegalStateException(
                "Issue template not found at location: $ISSUE_TEMPLATE_LOCATION"
            )
        }

        return templateStream.bufferedReader().use { it.readText() }
    }

    // TODO: Perhaps implement seeding for more sophisticated issue IDs.
    fun createIssueUuid(): UUID {
        return UUID.randomUUID()
    }
}