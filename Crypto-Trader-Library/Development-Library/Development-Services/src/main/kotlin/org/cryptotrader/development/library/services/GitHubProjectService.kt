package org.cryptotrader.development.library.services

import com.fasterxml.jackson.databind.JsonNode
import org.cryptotrader.development.library.communication.request.AddGitHubProjectIssueRequest
import org.cryptotrader.development.library.model.DevelopmentIssue
import org.cryptotrader.development.library.model.project.IssueType
import org.cryptotrader.development.library.model.project.ProjectItemType
import org.cryptotrader.development.library.services.client.exchange.GitHubGraphQlExchange
import org.cryptotrader.development.library.services.client.exchange.GitHubIssueExchange
import org.cryptotrader.development.library.services.client.exchange.GitHubProjectExchange
import org.cryptotrader.development.library.services.github.generated.model.ProjectsV2Field
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.let

@Service
open class GitHubProjectService(
    private val gitHubProjectExchange: GitHubProjectExchange,
    private val gitHubIssueExchange: GitHubIssueExchange,
    private val gitHubGraphQlExchange: GitHubGraphQlExchange,
    private val githubMetadataService: GitHubMetadataService,
    private val gitHubRepository: GHRepository,
    @Value($$"${github.repository}") private val repository: String,
    @Value($$"${github.projectNumber}") private val projectNumber: Int,
    @Value($$"${github.organization}") private val organization: String
) {
    companion object {
        const val REQUEST_TYPE_KEY = "Feature"
        val log: Logger = LoggerFactory.getLogger(GitHubProjectService::class.java)
    }

    fun getFields(): List<ProjectsV2Field> {
        return gitHubProjectExchange.getFields(
            this.organization,
            this.projectNumber
        )
    }

    fun addIssueToProject(issueItemId: Long): Long {
        val response: JsonNode = this.gitHubProjectExchange.addIssue(
            this.organization,
            this.projectNumber,
            AddGitHubProjectIssueRequest(
                id = issueItemId
            )
        )

        val projectItemId = response
            .path("id")

        check(projectItemId.isIntegralNumber) {
            "GitHub did not return a valid Project item ID: $response"
        }

        return projectItemId.longValue()
    }

    fun addTypeToIssue(createdIssue: GHIssue, issueType: IssueType) {
        val issueTypeId: String = when (issueType) {
            // TODO: Non-sensitive, but should be fetched instead of
            //       hardcoded.
            IssueType.TASK -> "IT_kwDOEK1RT84B-YfV"
            IssueType.BUG -> "IT_kwDOEK1RT84B-YfW"
            IssueType.FEATURE -> "IT_kwDOEK1RT84B-YfX"
        }
        val mutation = $$"""
        mutation UpdateIssueType(
            $issueId: ID!
            $issueTypeId: ID!
        ) {
            updateIssueIssueType(
                input: {
                    issueId: $issueId
                    issueTypeId: $issueTypeId
                }
            ) {
                issue {
                    id
                    issueType {
                        name
                    }
                }
            }
        }
    """.trimIndent()

        val response: JsonNode = this.gitHubGraphQlExchange.execute(
            mapOf(
                "query" to mutation,
                "variables" to mapOf(
                    "issueId" to createdIssue.nodeId,
                    "issueTypeId" to issueTypeId
                )
            )
        )

        require(response.path("errors").isMissingNode ||
            response.path("errors").isEmpty) {
            "GitHub GraphQL error: ${response.path("errors")}"
        }

        require(!response.path("data")
            .path("updateIssueIssueType")
            .path("issue")
            .isMissingNode) {
            "GitHub did not return an updated issue: $response"
        }
    }

    fun <T : DevelopmentIssue> addAllFieldsToIssue(createdIssue: GHIssue, issueModel: T, projectItemId: Long) {
        val fieldMap: MutableMap<ProjectItemType, Map<Long, String>> = mutableMapOf()

        issueModel.effort?.let { effort ->
            val effortFieldId: Long = this.githubMetadataService.getOrganizationFieldId(
                ProjectItemType.EFFORT.typeName
            )
            fieldMap[ProjectItemType.EFFORT] = mapOf(effortFieldId to effort.effortName)
        }

        issueModel.priority?.let { priority ->
            val priorityFieldId: Long =
                this.githubMetadataService.getOrganizationFieldId(
                    ProjectItemType.PRIORITY.typeName
                )
            fieldMap[ProjectItemType.PRIORITY] =
                mapOf(priorityFieldId to priority.priorityName)
        }

        if (fieldMap.isNotEmpty()) {
            val issueFieldValues: List<Map<String, Any>> = fieldMap.flatMap { (itemType, fieldIdValueMap) ->
                fieldIdValueMap.map { (fieldId, value) ->
                    mapOf(
                        "field_id" to fieldId,
                        "value" to value
                    )
                }
            }

            this.gitHubIssueExchange.updateIssue(
                this.organization,
                this.repository,
                createdIssue.number,
                request = mapOf(
                    ProjectItemType.TYPE.typeName.lowercase() to REQUEST_TYPE_KEY,
                    "issue_field_values" to issueFieldValues
                )
            )

            val fields: List<ProjectsV2Field> = this.gitHubProjectExchange.getFields(
                this.organization,
                this.projectNumber
            )
            val projectFieldMap: MutableMap<ProjectItemType, Map<Int, String>> = mutableMapOf()

            fields.forEach { field ->
                if (field.name.equals(ProjectItemType.ISSUE_PRIORITY.typeName, ignoreCase = true)) {
                    issueModel.projectPriority?.let { priority ->
                        val option = field.options!!.first {
                            it.name.raw.equals(priority.priorityName, ignoreCase = true)
                        }

                        projectFieldMap[ProjectItemType.ISSUE_PRIORITY] = mapOf(
                            field.id to option.id
                        )
                    }
                }

                if (field.name.equals(ProjectItemType.MODULE.typeName, ignoreCase = true)) {
                    issueModel.module?.let { module ->
                        val option = field.options!!.first {
                            it.name.raw.equals(module.moduleName, ignoreCase = true)
                        }

                        projectFieldMap[ProjectItemType.MODULE] = mapOf(
                            field.id to option.id
                        )
                    }
                }

                if (field.name.equals(ProjectItemType.SIZE.typeName, ignoreCase = true)) {
                    issueModel.size?.let { size ->
                        val option = field.options!!.first {
                            it.name.raw.equals(size.sizeName, ignoreCase = true)
                        }

                        projectFieldMap[ProjectItemType.SIZE] = mapOf(
                            field.id to option.id
                        )
                    }
                }
            }

            val projectFields: Map<Int, String> = projectFieldMap.flatMap { (_, fieldIdValueMap) ->
                fieldIdValueMap.map { (fieldId, value) ->
                    fieldId to value
                }
            }.toMap()

            if (projectFields.isNotEmpty()) {
                this.gitHubProjectExchange.updateItem(
                    this.organization,
                    this.projectNumber,
                    projectItemId,
                    requestBody = mapOf(
                        "fields" to projectFields.map { (fieldId, value) ->
                            mapOf(
                                "id" to fieldId,
                                "value" to value
                            )
                        }
                    )
                )
            }

            issueModel.type?.let { type ->
                this.addTypeToIssue(createdIssue, type)
            }
        } else {
            log.info("No fields to update for issue #${createdIssue.number}. Skipping field update.")
        }
    }
}