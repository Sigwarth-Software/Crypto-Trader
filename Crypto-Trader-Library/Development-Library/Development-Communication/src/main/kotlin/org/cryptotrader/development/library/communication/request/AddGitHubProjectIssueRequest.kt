package org.cryptotrader.development.library.communication.request

import org.cryptotrader.development.library.model.project.ProjectItemType

class AddGitHubProjectIssueRequest(
    override val id: Long,
) : AddGitHubProjectItemRequest(
    id, ProjectItemType.ISSUE.typeName
)