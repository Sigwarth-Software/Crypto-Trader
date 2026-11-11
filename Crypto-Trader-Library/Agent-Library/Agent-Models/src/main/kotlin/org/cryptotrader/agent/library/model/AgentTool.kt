package org.cryptotrader.agent.library.model

enum class AgentTool {
    READ_FILE,
    LIST_DIRECTORIES,
    SEARCH_TEXT_IN_FILES,
    SEARCH_FILES,
    READ_MANY_FILES,
    RESOLVE_PATH,
    LIST_ALLOWED_TABLES,
    DESCRIBE_TABLE,
    FETCH_WEBSITE_TEXT,
    GET_ISSUE_CREATION_PROMPT,
    CREATE_ISSUE,
    CONFIRM_ISSUE,
    PUBLISH_ISSUE;

    companion object {
        fun fromMethodName(methodName: String): AgentTool {
            return when (methodName) {
                "readFile" -> READ_FILE
                "listDirectories" -> LIST_DIRECTORIES
                "searchTextInFiles" -> SEARCH_TEXT_IN_FILES
                "searchFiles" -> SEARCH_FILES
                "readManyFiles" -> READ_MANY_FILES
                "resolvePath" -> RESOLVE_PATH
                "listAllowedTables" -> LIST_ALLOWED_TABLES
                "describeTable" -> DESCRIBE_TABLE
                "fetchWebsiteText" -> FETCH_WEBSITE_TEXT
                "getIssueCreationPrompt" -> GET_ISSUE_CREATION_PROMPT
                "createIssue" -> CREATE_ISSUE
                "confirmIssue" -> CONFIRM_ISSUE
                "publishIssue" -> PUBLISH_ISSUE
                else -> throw IllegalArgumentException("Invalid method name: $methodName")
            }
        }
    }
}
