package org.cryptotrader.agent.library.component

import java.util.UUID
import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.development.library.model.DevelopmentIssue
import org.cryptotrader.development.library.model.UserStoryIssue
import org.cryptotrader.development.library.services.GitHubIssueService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify

@DisplayName("GitHubIssueTool")
@UnitSuite
class GitHubIssueToolTest : CryptoTraderTest() {
    @Mock
    lateinit var properties: AgentConstraintsProperties

    @Mock
    lateinit var gitHubIssueService: GitHubIssueService

    @InjectMocks
    lateinit var gitHubIssueTool: GitHubIssueTool

    private val validTicketRequest: String = """
        As a developer, I want a sample feature.

        - [ ] Task one.
        - [ ] Task two.

        Points: 5 (hours)
    """.trimIndent()

    lateinit var ticketRequest: String

    private fun <T> anyIssue(): T {
        ArgumentMatchers.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    @Nested
    @DisplayName("parseTicketRequest")
    inner class ParseTicketRequest {
        @Test
        @DisplayName("should extract title, tasks, and points")
        fun extractTitleTasksAndPoints() {
            val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(validTicketRequest)
            assertEquals("As a developer, I want a sample feature.", issue.issueTitle)
            assertEquals(5, issue.points)
            assertTrue(issue.issueDescription.contains("Task one."))
            assertTrue(issue.issueDescription.contains("Task two."))
        }

        @Nested
        @DisplayName("when labels are present")
        inner class WhenLabelsArePresent {
            @BeforeEach
            fun setUp() {
                ticketRequest = """
                As a developer, I want a sample feature.

                - [ ] Task one.

                Labels: bug, needs triage
                Points: 5 (hours)
            """.trimIndent()
            }

            @Test
            @DisplayName("should extract labels")
            fun extractLabels() {
                val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(ticketRequest)
                assertEquals(listOf("bug", "needs triage"), issue.labels)
            }
        }

        @Nested
        @DisplayName("when the template has front matter and an explicit title")
        inner class WhenTheTemplateHasFrontMatterAndAnExplicitTitle {
            private lateinit var templateDraft: String

            @BeforeEach
            fun setUp() {
                templateDraft = """
                ---
                name: Agile User Story
                about: Create a concise user story with a task checklist and an hours-based point estimate
                title: "As a trader, I want real-time order execution."
                labels: "needs triage, user story"
                assignees: ""
                ---

                <!--
                Multi-line comment instructions
                here
                -->

                As a trader, I want real-time order execution.

                - [ ] Implement order routing service.
                - [ ] Add unit tests for latency bounds.

                Points: 8 (hours)
            """.trimIndent()
            }

            @DisplayName("should parse the title, tasks, points, and labels")
            @Test
            fun parseTemplateWithFrontMatter() {
                val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(templateDraft)

                assertEquals("As a trader, I want real-time order execution.", issue.issueTitle)
                assertEquals(listOf("needs triage", "user story"), issue.labels)
                assertEquals(8, issue.points)
            }
        }

        @Nested
        @DisplayName("when the front matter has no explicit title")
        inner class WhenTheFrontMatterHasNoExplicitTitle {
            private lateinit var templateDraft: String

            @BeforeEach
            fun setUp() {
                templateDraft = """
                ---
                name: Agile User Story
                about: Create a concise user story with a task checklist and an hours-based point estimate
                labels: "needs triage, user story"
                assignees: ""
                ---

                <!-- Template drafting instructions -->

                As a trader, I want real-time order execution.

                - [ ] Implement order routing service.

                Points: 4 (hours)
            """.trimIndent()
            }

            @DisplayName("should use the first content line as the title")
            @Test
            fun useFirstContentLineAsTitle() {
                val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(templateDraft)

                assertEquals("As a trader, I want real-time order execution.", issue.issueTitle)
                assertEquals(listOf("needs triage", "user story"), issue.labels)
                assertEquals(4, issue.points)
            }
        }

        @Nested
        @DisplayName("when labels are quoted")
        inner class WhenLabelsAreQuoted {
            private lateinit var ticketRequest: String

            @BeforeEach
            fun setUp() {
                ticketRequest = """
                As a developer, I want a sample feature.

                - [ ] Task one.

                Labels: "bug", 'user story', documentation
                Points: 3 (hours)
            """.trimIndent()
            }

            @DisplayName("should unquote labels")
            @Test
            fun unquoteLabels() {
                val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(ticketRequest)
                assertEquals(listOf("bug", "user story", "documentation"), issue.labels)
            }
        }

        @DisplayName("when points are missing")
        @Nested
        inner class WhenPointsAreMissing {

            @BeforeEach
            fun setUp() {
                ticketRequest = """
                As a developer, I want a sample feature.

                - [ ] Task one.
            """.trimIndent()
            }

            @Test
            @DisplayName("should default points to zero")
            fun defaultPointsToZero() {
                val issue: UserStoryIssue =
                    gitHubIssueTool.parseTicketRequest(ticketRequest)
                assertEquals(0, issue.points)
            }
        }

        @DisplayName("when labels are missing")
        @Nested
        inner class WhenLabelsAreMissing {
            @Test
            @DisplayName("should default labels to empty list")
            fun defaultLabelsToEmptyList() {
                val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(validTicketRequest)
                assertTrue(issue.labels.isEmpty())
            }
        }

        @Nested
        @DisplayName("when no tasks are present")
        inner class WhenNoTasksPresent {
            @Test
            @DisplayName("should throw exception")
            fun throwException() {
                assertThrows(IllegalArgumentException::class.java) {
                    gitHubIssueTool.parseTicketRequest("As a developer, I want a sample feature.")
                }
            }
        }

        @Nested
        @DisplayName("when a title prefix is found")
        inner class WhenTitlePrefixFound {
            @BeforeEach
            fun setUp() {
                ticketRequest = """
                Title: As a developer, I want a sample feature.

                - [ ] Task one.
            """.trimIndent()
            }

            @Test
            @DisplayName("should strip title prefix")
            fun stripTitlePrefix() {
                val issue: UserStoryIssue =
                    gitHubIssueTool.parseTicketRequest(ticketRequest)
                assertEquals(
                    "As a developer, I want a sample feature.",
                    issue.issueTitle
                )
            }
        }

        @Nested
        @DisplayName("when the issue prefix is found")
        inner class WhenIssuePrefixFound {
            @BeforeEach
            fun setUp() {
                ticketRequest = """
                Issue: As a developer, I want a sample feature.

                - [ ] Task one.
            """.trimIndent()
            }

            @Test
            @DisplayName("should strip the issue prefix")
            fun stripTheIssuePrefix() {
                val issue: UserStoryIssue =
                    gitHubIssueTool.parseTicketRequest(ticketRequest)
                assertEquals(
                    "As a developer, I want a sample feature.",
                    issue.issueTitle
                )
            }
        }
    }

    @Nested
    @DisplayName("requireNoSensitiveContent")
    inner class RequireNoSensitiveContent {
        @Nested
        @DisplayName("when an API key is present")
        inner class WhenAnAPIKeyIsPresent {
            @DisplayName("should throw SecurityException")
            @Test
            fun throwSecurityException() {
                assertThrows(SecurityException::class.java) {
                    gitHubIssueTool.requireNoSensitiveContent("api_key: abc123xyz")
                }
            }
        }

        @Nested
        @DisplayName("when the content has no sensitive data")
        inner class WhenTheContentHasNoSensitiveData {
            @DisplayName("should accept safe content")
            @Test
            fun acceptSafeContent() {
                gitHubIssueTool.requireNoSensitiveContent(validTicketRequest)
            }
        }

    }

    @Nested
    @DisplayName("getIssueCreationPrompt")
    inner class GetIssueCreationPrompt {
        @DisplayName("should return the prompt from the service")
        @Test
        fun returnPromptFromService() {
            `when`(gitHubIssueService.getIssueCreationPrompt(
                "Add a new feature"
            )).thenReturn("Generated prompt")
            val result: String = gitHubIssueTool.getIssueCreationPrompt("Add a new feature")

            assertEquals("Generated prompt", result)
        }

        @Nested
        @DisplayName("when the prompt is blank")
        inner class WhenThePromptIsBlank {
            @DisplayName("should throw IllegalArgumentException")
            @Test
            fun throwIllegalArgumentException() {
                assertThrows(IllegalArgumentException::class.java) {
                    gitHubIssueTool.getIssueCreationPrompt("   ")
                }
            }
        }
    }

    @DisplayName("createIssue")
    @Nested
    inner class CreateIssue {
        @Nested
        @DisplayName("when the ticket request is valid")
        inner class WhenTheTicketRequestIsValid {
            private lateinit var result: String

            @BeforeEach
            fun setUp() {
                `when`(gitHubIssueService.createIssueUuid()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                `when`(gitHubIssueService.presentIssue(anyIssue<DevelopmentIssue>())).thenReturn("Presented issue")

                result = gitHubIssueTool.createIssue(validTicketRequest)
            }

            @DisplayName("should return the proposed issue identifier")
            @Test
            fun returnProposedIssueIdentifier() {
                assertTrue(result.contains("Presented issue"))
                assertTrue(result.contains("00000000-0000-0000-0000-000000000001"))
            }
        }

        @Nested
        @DisplayName("when the ticket request is blank")
        inner class WhenTheTicketRequestIsBlank {
            @DisplayName("should throw IllegalArgumentException")
            @Test
            fun throwIllegalArgumentException() {
                assertThrows(IllegalArgumentException::class.java) {
                    gitHubIssueTool.createIssue("   ")
                }
            }
        }
    }

    @DisplayName("confirmIssue")
    @Nested
    inner class ConfirmIssue {
        @Nested
        @DisplayName("when the issue was proposed")
        inner class WhenTheIssueWasProposed {
            private lateinit var issueId: UUID

            private lateinit var result: String

            @BeforeEach
            fun setUp() {
                issueId = UUID.fromString("00000000-0000-0000-0000-000000000002")
                `when`(gitHubIssueService.createIssueUuid()).thenReturn(issueId)
                `when`(gitHubIssueService.presentIssue(anyIssue<DevelopmentIssue>())).thenReturn("Presented issue")
                gitHubIssueTool.createIssue(validTicketRequest)

                result = gitHubIssueTool.confirmIssue(issueId.toString())
            }

            @DisplayName("should confirm the proposed issue")
            @Test
            fun confirmProposedIssue() {
                assertTrue(result.contains("confirmed"))
            }
        }

        @Nested
        @DisplayName("when the issue identifier is unknown")
        inner class WhenTheIssueIdentifierIsUnknown {
            @DisplayName("should throw IllegalArgumentException")
            @Test
            fun throwIllegalArgumentException() {
                assertThrows(IllegalArgumentException::class.java) {
                    gitHubIssueTool.confirmIssue(UUID.randomUUID().toString())
                }
            }
        }

        @Nested
        @DisplayName("when the issue identifier is malformed")
        inner class WhenTheIssueIdentifierIsMalformed {
            @DisplayName("should throw IllegalArgumentException")
            @Test
            fun throwIllegalArgumentException() {
                assertThrows(IllegalArgumentException::class.java) {
                    gitHubIssueTool.confirmIssue("not-a-uuid")
                }
            }
        }
    }

    @DisplayName("publishIssue")
    @Nested
    inner class PublishIssue {
        @Nested
        @DisplayName("when the issue was confirmed")
        inner class WhenTheIssueWasConfirmed {
            private lateinit var result: String

            @BeforeEach
            fun setUp() {
                val issueId = UUID.fromString("00000000-0000-0000-0000-000000000003")
                `when`(gitHubIssueService.createIssueUuid()).thenReturn(issueId)
                `when`(gitHubIssueService.presentIssue(anyIssue<DevelopmentIssue>())).thenReturn("Presented issue")
                `when`(gitHubIssueService.publishIssue(anyIssue<DevelopmentIssue>())).thenReturn("https://github.com/example/repo/issues/1")
                gitHubIssueTool.createIssue(validTicketRequest)
                gitHubIssueTool.confirmIssue(issueId.toString())

                result = gitHubIssueTool.publishIssue()
            }

            @DisplayName("should publish the confirmed issue")
            @Test
            fun publishConfirmedIssue() {
                assertTrue(result.contains("https://github.com/example/repo/issues/1"))
                verify(gitHubIssueService).publishIssue(anyIssue())
            }
        }

        @Nested
        @DisplayName("when no issue is confirmed")
        inner class WhenNoIssueIsConfirmed {
            @DisplayName("should throw IllegalStateException")
            @Test
            fun throwIllegalStateException() {
                assertThrows(IllegalStateException::class.java) {
                    gitHubIssueTool.publishIssue()
                }
            }
        }
    }
}
