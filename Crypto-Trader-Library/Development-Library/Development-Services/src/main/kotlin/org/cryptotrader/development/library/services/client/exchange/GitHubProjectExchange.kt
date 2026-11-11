package org.cryptotrader.development.library.services.client.exchange

import com.fasterxml.jackson.databind.JsonNode
import org.cryptotrader.development.library.communication.request.AddGitHubProjectIssueRequest
import org.cryptotrader.development.library.services.github.generated.model.ProjectsV2
import org.cryptotrader.development.library.services.github.generated.model.ProjectsV2Field
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PatchExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/orgs/{organization}/projectsV2/{projectNumber}")
interface GitHubProjectExchange {
    @GetExchange
    fun getProject(
        @PathVariable("organization") organization: String,
        @PathVariable("projectNumber") projectNumber: Int,
    ): ProjectsV2

    @PostExchange(
        url = "/items",
        contentType = MediaType.APPLICATION_JSON_VALUE,
    )
    fun addIssue(
        @PathVariable("organization") organization: String,
        @PathVariable("projectNumber") projectNumber: Int,
        @RequestBody request: AddGitHubProjectIssueRequest,
    ): JsonNode

    @GetExchange("/fields")
    fun getFields(
        @PathVariable("organization") organization: String,
        @PathVariable("projectNumber") projectNumber: Int,
    ): List<ProjectsV2Field>

    @PatchExchange(
        url = "/items/{itemId}",
        contentType = MediaType.APPLICATION_JSON_VALUE,
    )
    fun updateItem(
        @PathVariable("organization") organization: String,
        @PathVariable("projectNumber") projectNumber: Int,
        @PathVariable("itemId") itemId: Long,
        @RequestBody requestBody: Map<String, Any>,
    ): ResponseEntity<Void>
}