package org.cryptotrader.development.library.services.client.exchange

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/orgs/{organization}")
interface GitHubOrganizationExchange {
    @GetExchange("/issue-fields")
    fun getIssueFields(
        @PathVariable("organization") organization: String,
    ): JsonNode
}