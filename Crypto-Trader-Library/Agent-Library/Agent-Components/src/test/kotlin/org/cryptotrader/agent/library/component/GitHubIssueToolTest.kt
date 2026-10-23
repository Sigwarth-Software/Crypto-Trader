package org.cryptotrader.agent.library.component

import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.development.library.model.DevelopmentIssue
import org.cryptotrader.development.library.model.UserStoryIssue
import org.cryptotrader.development.library.services.GitHubIssueService
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
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
import java.util.UUID

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
            fun defaultsPointsToZero() {
                val issue: UserStoryIssue =
                    gitHubIssueTool.parseTicketRequest(ticketRequest)
                assertEquals(0, issue.points)
            }
        }

        @Test
        @DisplayName("should extract labels when present")
        fun extractLabelsWhenPresent() {
            ticketRequest = """
                As a developer, I want a sample feature.

                - [ ] Task one.

                Labels: bug, needs triage
                Points: 5 (hours)
            """.trimIndent()
            val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(ticketRequest)
            assertEquals(listOf("bug", "needs triage"), issue.labels)
        }

        @Nested
        inner class WhenLabelsAreMissing {
            @Test
            @DisplayName("should default labels to empty list")
            fun defaultsToAnEmptyList() {
                val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(validTicketRequest)
                assertTrue(issue.labels.isEmpty())
            }
        }

        @Nested
        @DisplayName("when no tasks found")
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
        @DisplayName("when a title prefix found")
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
            fun stripIssuePrefix() {
                val issue: UserStoryIssue =
                    gitHubIssueTool.parseTicketRequest(ticketRequest)
                assertEquals(
                    "As a developer, I want a sample feature.",
                    issue.issueTitle
                )
            }
        }
        @Test
        fun parsesTicketRequest_WithFullTemplateAndFrontMatter() {
            val templateDraft = """
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

            val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(templateDraft)

            assertEquals("As a trader, I want real-time order execution.", issue.issueTitle)
            assertEquals(listOf("needs triage", "user story"), issue.labels)
            assertEquals(8, issue.points)
        }

        @Test
        fun parsesTicketRequest_WithFrontMatterWithoutExplicitTitle() {
            val templateDraft = """
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

            val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(templateDraft)

            assertEquals("As a trader, I want real-time order execution.", issue.issueTitle)
            assertEquals(listOf("needs triage", "user story"), issue.labels)
            assertEquals(4, issue.points)
        }

        @Test
        fun unquotesLabelsCorrectly() {
            val ticketRequest = """
                As a developer, I want a sample feature.

                - [ ] Task one.

                Labels: "bug", 'user story', documentation
                Points: 3 (hours)
            """.trimIndent()
            val issue: UserStoryIssue = gitHubIssueTool.parseTicketRequest(ticketRequest)
            assertEquals(listOf("bug", "user story", "documentation"), issue.labels)
        }
    }

    @Nested
    @DisplayName("requireNoSensitiveContent")
    inner class RequireNoSensitiveContent {
        @Test
        fun throwsSecurityException_WhenApiKeyPresent() {
            assertThrows(SecurityException::class.java) {
                gitHubIssueTool.requireNoSensitiveContent("api_key: abc123xyz")
            }
        }

        @Test
        fun doesNotThrow_WhenContentIsClean() {
            gitHubIssueTool.requireNoSensitiveContent(validTicketRequest)
        }
    }

    @Nested
    @DisplayName("getIssueCreationPrompt")
    inner class GetIssueCreationPrompt {
        @Test
        fun returnsPromptFromService() {
            `when`(gitHubIssueService.getIssueCreationPrompt(
                "Add a new feature"
            )).thenReturn("Generated prompt")
            val result: String = gitHubIssueTool.getIssueCreationPrompt("Add a new feature")

            assertEquals("Generated prompt", result)
        }

        @Test
        fun throwsException_WhenPromptBlank() {
            assertThrows(IllegalArgumentException::class.java) {
                gitHubIssueTool.getIssueCreationPrompt("   ")
            }
        }
    }

    @Nested
    inner class CreateIssue {
        @Test
        fun createsIssueAndReturnsIdentifier() {
            `when`(gitHubIssueService.createIssueUuid()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            `when`(gitHubIssueService.presentIssue(anyIssue<DevelopmentIssue>())).thenReturn("Presented issue")

            val result: String = gitHubIssueTool.createIssue(validTicketRequest)

            assertTrue(result.contains("Presented issue"))
            assertTrue(result.contains("00000000-0000-0000-0000-000000000001"))
        }

        @Test
        fun throwsException_WhenTicketRequestBlank() {
            assertThrows(IllegalArgumentException::class.java) {
                gitHubIssueTool.createIssue("   ")
            }
        }
    }

    @Nested
    inner class ConfirmIssue {
        @Test
        fun confirmsPreviouslyProposedIssue() {
            val issueId = UUID.fromString("00000000-0000-0000-0000-000000000002")
            `when`(gitHubIssueService.createIssueUuid()).thenReturn(issueId)
            `when`(gitHubIssueService.presentIssue(anyIssue<DevelopmentIssue>())).thenReturn("Presented issue")

            gitHubIssueTool.createIssue(validTicketRequest)
            val result: String = gitHubIssueTool.confirmIssue(issueId.toString())

            assertTrue(result.contains("confirmed"))
        }

        @Test
        fun throwsException_WhenIssueIdUnknown() {
            assertThrows(IllegalArgumentException::class.java) {
                gitHubIssueTool.confirmIssue(UUID.randomUUID().toString())
            }
        }

        @Test
        fun throwsException_WhenIssueIdMalformed() {
            assertThrows(IllegalArgumentException::class.java) {
                gitHubIssueTool.confirmIssue("not-a-uuid")
            }
        }
    }

    @Nested
    inner class PublishIssue {
        @Test
        fun publishesConfirmedIssue() {
            val issueId = UUID.fromString("00000000-0000-0000-0000-000000000003")
            `when`(gitHubIssueService.createIssueUuid()).thenReturn(issueId)
            `when`(gitHubIssueService.presentIssue(anyIssue<DevelopmentIssue>())).thenReturn("Presented issue")
            `when`(gitHubIssueService.publishIssue(anyIssue<DevelopmentIssue>())).thenReturn("https://github.com/example/repo/issues/1")

            gitHubIssueTool.createIssue(validTicketRequest)
            gitHubIssueTool.confirmIssue(issueId.toString())
            val result: String = gitHubIssueTool.publishIssue()

            assertTrue(result.contains("https://github.com/example/repo/issues/1"))
            verify(gitHubIssueService).publishIssue(anyIssue())
        }

        @Test
        fun throwsException_WhenNoIssueConfirmed() {
            assertThrows(IllegalStateException::class.java) {
                gitHubIssueTool.publishIssue()
            }
        }
    }
}
