package org.cryptotrader.development.library.services

import com.fasterxml.jackson.databind.JsonNode
import org.cryptotrader.development.library.services.client.exchange.GitHubOrganizationExchange
import org.kohsuke.github.GHLabel
import org.kohsuke.github.GHRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
open class GitHubMetadataService @Autowired constructor(
    private val repository: GHRepository,
    private val githubOrganizationExchange: GitHubOrganizationExchange,
    @Value($$"${github.organization}") private val organization: String
) {
    private var cachedLabels: List<GHLabel>? = null

    fun getOrganizationFieldId(fieldName: String): Long {
        val fields: JsonNode =
            this.githubOrganizationExchange.getIssueFields(
                this.organization
            )

        val field: JsonNode = fields.firstOrNull {
            it.path("name").asText().equals(fieldName, ignoreCase = true)
        } ?: throw IllegalArgumentException(
            "Organization issue field '$fieldName' not found."
        )

        return field.path("id").asLong()
    }

    fun getAvailableLabelsNames(): List<String> {
        return this.getAvailableLabels().map { it.name }
    }

    fun getAvailableLabels(): List<GHLabel> {
        return this.cachedLabels ?: this.repository.listLabels().toList().also {
            this.cachedLabels = it
        }
    }

    fun refreshAvailableLabels(): List<GHLabel> {
        this.cachedLabels = null
        return this.getAvailableLabels()
    }
}