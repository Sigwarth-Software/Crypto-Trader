package org.cryptotrader.development.library.config

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.development.library.model.UserStoryIssue
import org.cryptotrader.development.library.model.project.GitHubProjectModule
import org.cryptotrader.development.library.model.project.IssueEffort
import org.cryptotrader.development.library.model.project.IssuePriority
import org.cryptotrader.development.library.model.project.IssueSize
import org.cryptotrader.development.library.model.project.IssueType
import org.cryptotrader.development.library.model.project.ProjectIssuePriority
import org.cryptotrader.development.library.services.GitHubIssueService
import org.cryptotrader.development.library.services.GitHubMetadataService
import org.cryptotrader.development.library.services.GitHubProjectService
import org.cryptotrader.development.library.services.client.exchange.GitHubGraphQlExchange
import org.cryptotrader.development.library.services.client.exchange.GitHubIssueExchange
import org.cryptotrader.development.library.services.client.exchange.GitHubOrganizationExchange
import org.cryptotrader.development.library.services.client.exchange.GitHubProjectExchange
import org.cryptotrader.testing.library.infrastructure.annotation.EndToEndSuite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.springframework.web.client.RestClient

@DisplayName("UserStoryIssuePublishingE2E")
@Tag("live-github")
@EnabledIfEnvironmentVariable(named = "RUN_GITHUB_PUBLISH_E2E", matches = "true")
@EndToEndSuite
class UserStoryIssuePublishingE2ETest {
    @Nested
    @DisplayName("publishIssue")
    inner class PublishIssue {
        @Nested
        @DisplayName("when live publishing is explicitly enabled")
        inner class WhenLivePublishingIsExplicitlyEnabled {
            private lateinit var issue: UserStoryIssue
            private lateinit var repository: GHRepository
            private lateinit var issueService: GitHubIssueService
            private lateinit var expectedLabels: Set<String>

            @BeforeEach
            fun setUp() {
                issue = UserStoryIssue(
                    issueTitle = "[E2E] Publish a user story (${Instant.now()})",
                    tasks = listOf(
                        "Publish a UserStoryIssue using the configured GitHub credentials",
                        "Add the published issue to the configured GitHub project"
                    ),
                    labels = listOf("enhancement"),
                    points = 1,
                    extraContent = "Created by the manually enabled UserStoryIssue publishing test.",
                    modules = GitHubProjectModule.CRYPTO_TRADER_DEVELOPMENT,
                    priority = IssuePriority.MEDIUM,
                    projectPriority = ProjectIssuePriority.BACKLOG,
                    effort = IssueEffort.MEDIUM,
                    type = IssueType.TASK,
                    size = IssueSize.XL
                )
                val token: String? = getEnvVariable("GH_TOKEN")
                require(token != null) { "Set GH_TOKEN before enabling the live publishing test." }
                val projectNumber: String = getEnvVariable("GITHUB_PROJECT_NUMBER") ?: "1"
                require(projectNumber.toIntOrNull()?.let { it > 0 } == true) {
                    "GITHUB_PROJECT_NUMBER must be a positive integer."
                }
                val properties = GitHubProperties(
                    token = token,
                    repository = getEnvVariable("GITHUB_REPOSITORY") ?: "Crypto-Trader",
                    organization = getEnvVariable("GITHUB_ORGANIZATION") ?: "Sigwarth-Software",
                    projectNumber = projectNumber.toInt()
                )
                val configuration = GitHubConfiguration()
                val github: GitHub = configuration.github(properties)
                repository = configuration.cryptoTraderRepository(github, properties)
                val exchange: GitHubProjectExchange = GitHubProjectConfiguration().githubProjectExchange(RestClient.builder(), properties)
                val issueExchange: GitHubIssueExchange = GitHubProjectConfiguration().githubIssueExchange(RestClient.builder(), properties)
                val organizationExchange: GitHubOrganizationExchange = GitHubProjectConfiguration().githubOrganizationExchange(RestClient.builder(), properties)
                val graphqlExchange: GitHubGraphQlExchange = GitHubProjectConfiguration().githubGraphQlExchange(RestClient.builder(), properties)
                val metadataService = GitHubMetadataService(
                    repository,
                    organizationExchange,
                    properties.organization
                )
                val projectService = GitHubProjectService(
                    exchange,
                    issueExchange,
                    graphqlExchange,
                    metadataService,
                    repository,
                    properties.repository,
                    properties.projectNumber,
                    properties.organization,
                )
                issueService = GitHubIssueService(
                    repository,
                    metadataService,
                    projectService
                )
                exchange.getProject(properties.organization, properties.projectNumber)
                expectedLabels = issueService.validateLabels(issue.labels).toSet()
                println("Publishing to ${properties.repository}, project ${properties.organization}/${properties.projectNumber}")
            }

            @DisplayName("should publish a user story issue with real credentials")
            @Test
            fun publishUserStoryIssue() {
                val url = issueService.publishIssue(issue)
                println("Published UserStoryIssue: $url")

                val number: Int = url.substringAfterLast('/').toInt()
                val savedIssue: GHIssue = repository.getIssue(number)
                assertThat(savedIssue.htmlUrl.toString()).isEqualTo(url)
                assertThat(savedIssue.title).isEqualTo(issue.issueTitle)
                assertThat(savedIssue.body)
                    .startsWith(issue.getDescription().substringBefore("_This content was generated"))
                    .contains("_This content was generated by Crypto-Trader-Development at ")
                assertThat(savedIssue.labels.map { it.name }.toSet()).isEqualTo(expectedLabels)
            }
        }
    }

    private fun getEnvVariable(name: String): String? {
        return System.getenv(name)?.trim()?.takeIf { it.isNotEmpty() }
    }
}
