package org.cryptotrader.agent.library.model.file

data class FileMetadata(
    val path: String,
    val size: Long,
    val lastModified: String,
    val isDirectory: Boolean,
    val lineCount: Long? = null
)
