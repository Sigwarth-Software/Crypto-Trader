package org.cryptotrader.development.library.config

import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(GitHubProperties::class)
@Configuration(proxyBeanMethods = false)
open class GitHubConfiguration {
    @Bean
    open fun github(
        properties: GitHubProperties
    ): GitHub {
        return GitHubBuilder()
            .withOAuthToken(properties.token)
            .build()
    }

    @Bean
    open fun cryptoTraderRepository(
        github: GitHub,
        properties: GitHubProperties
    ): GHRepository {
        return github.getRepository(properties.repository)
    }
}