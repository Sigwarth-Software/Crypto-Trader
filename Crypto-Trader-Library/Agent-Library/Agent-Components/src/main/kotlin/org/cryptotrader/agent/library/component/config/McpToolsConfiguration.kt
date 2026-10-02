package org.cryptotrader.agent.library.component.config

import org.cryptotrader.agent.library.component.DatabaseReaderTool
import org.cryptotrader.agent.library.component.FileReaderTool
import org.cryptotrader.agent.library.component.GitHubIssueTool
import org.cryptotrader.agent.library.component.HttpFetchTool
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
open class McpToolsConfiguration {

    @Bean
    fun toolCallbackProvider(
        safeFileReadTool: FileReaderTool,
        safeHttpFetchTool: HttpFetchTool,
        safeDatabaseIntrospectionTool: DatabaseReaderTool,
        githubIssueTool: GitHubIssueTool
    ): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(
                safeFileReadTool,
                safeHttpFetchTool,
                safeDatabaseIntrospectionTool,
                githubIssueTool
            )
            .build()
    }
}