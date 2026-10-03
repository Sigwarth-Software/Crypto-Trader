package org.cryptotrader.development.library.services

import org.kohsuke.github.GHLabel
import org.kohsuke.github.GHRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
open class GitHubMetadataService @Autowired constructor(
    private val repository: GHRepository
) {
    private var cachedLabels: List<GHLabel>? = null

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