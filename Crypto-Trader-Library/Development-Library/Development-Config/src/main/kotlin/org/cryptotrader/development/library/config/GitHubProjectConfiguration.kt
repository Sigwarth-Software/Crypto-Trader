package org.cryptotrader.development.library.config

import org.cryptotrader.development.library.services.client.exchange.GitHubIssueExchange
import org.cryptotrader.development.library.services.client.exchange.GitHubProjectExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class GitHubProjectConfiguration {
    @Bean
    fun githubProjectExchange(
        restClientBuilder: RestClient.Builder,
        properties: GitHubProperties,
    ): GitHubProjectExchange {
        val factory: HttpServiceProxyFactory =
            this.getBaseClient(restClientBuilder, properties)

        return factory.createClient<GitHubProjectExchange>()
    }

    @Bean
    fun githubIssueExchange(
        restClientBuilder: RestClient.Builder,
        properties: GitHubProperties,
    ): GitHubIssueExchange {
        val factory: HttpServiceProxyFactory =
            this.getBaseClient(restClientBuilder, properties)

        return factory.createClient<GitHubIssueExchange>()
    }

    fun getBaseClient(
        restClientBuilder: RestClient.Builder,
        properties: GitHubProperties
    ): HttpServiceProxyFactory {
        val restClient: RestClient = restClientBuilder
            .baseUrl("https://api.github.com")
            .defaultHeaders { headers ->
                headers.setBearerAuth(properties.token)

                headers.set("Accept", "application/vnd.github+json")
                headers.set("X-GitHub-Api-Version", "2026-03-10")
            }.build()

        val adapter: RestClientAdapter = RestClientAdapter.create(restClient)
        val factory: HttpServiceProxyFactory =
            HttpServiceProxyFactory.builderFor(adapter).build()
        return factory
    }
}