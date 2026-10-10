package org.cryptotrader.development.library.services

import com.fasterxml.jackson.databind.JsonNode
import org.cryptotrader.development.library.communication.request.AddGitHubProjectIssueRequest
import org.cryptotrader.development.library.model.project.ProjectItemType
import org.cryptotrader.development.library.services.client.exchange.GitHubIssueExchange
import org.cryptotrader.development.library.services.client.exchange.GitHubProjectExchange
import org.cryptotrader.development.library.services.github.generated.model.ProjectsV2Field
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
open class GitHubProjectService(
    private val gitHubProjectExchange: GitHubProjectExchange,
    private val gitHubIssueExchange: GitHubIssueExchange,
    private val gitHubRepository: GHRepository,
    @Value($$"${github.repository}") private val repository: String,
    @Value($$"${github.projectNumber}") private val projectNumber: Int,
    @Value($$"${github.organization}") private val organization: String
) {
    fun getFields(): List<ProjectsV2Field> {
        return gitHubProjectExchange.getFields(
            this.organization,
            this.projectNumber
        )
    }

    fun addIssueToProject(issueItemId: Long) {
        val response: JsonNode = this.gitHubProjectExchange.addIssue(
            this.organization,
            this.projectNumber,
            AddGitHubProjectIssueRequest(
                id = issueItemId
            )
        )
    }

    fun addTypeToIssue(createdIssue: GHIssue) {
     this.gitHubIssueExchange.updateIssue(
         this.organization,
         this.repository,
         createdIssue.number,
         mapOf(
             ProjectItemType.TYPE.typeName.lowercase() to "Feature"
         )
     )
    }
}