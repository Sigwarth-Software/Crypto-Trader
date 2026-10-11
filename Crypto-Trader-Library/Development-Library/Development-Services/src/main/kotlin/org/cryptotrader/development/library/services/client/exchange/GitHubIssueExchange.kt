package org.cryptotrader.development.library.services.client.exchange

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PatchExchange

@HttpExchange("/repos/{owner}/{repository}/issues")
interface GitHubIssueExchange {
    @PatchExchange("/{issueNumber}")
    fun updateIssue(
        @PathVariable("owner") owner: String,
        @PathVariable("repository") repository: String,
        @PathVariable("issueNumber") issueNumber: Int,
        @RequestBody request: Map<String, Any>,
    )
}