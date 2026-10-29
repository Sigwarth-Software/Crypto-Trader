package org.cryptotrader.agent.library.component

import info.debatty.java.stringsimilarity.Levenshtein
import org.cryptotrader.agent.library.communication.response.DirectoryListingResponse
import org.cryptotrader.agent.library.communication.response.FileContentResponse
import org.cryptotrader.agent.library.communication.response.FileSearchResponse
import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.agent.library.model.file.FileMetadata
import org.cryptotrader.agent.library.model.file.FileSearchResult
import org.cryptotrader.security.library.infrastructure.annotation.UserRestricted
import org.cryptotrader.api.library.entity.user.UserRoleTier
import org.springaicommunity.mcp.annotation.McpToolParam
import org.springframework.ai.tool.annotation.Tool
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.*

@Component
class FileReaderTool @Autowired constructor(
    private val properties: AgentConstraintsProperties
) {

    private val allowedRoot: Path by lazy {
        Paths.get(properties.allowedRoot).toAbsolutePath().normalize()
    }

    @Tool(description = "Read a UTF-8 text file within the project directory. Supports line-based reading and safety limits.")
    @UserRestricted
    fun readFile(
        @McpToolParam(description = "The relative path to the file from the project root")
        relativePath: String,
        @McpToolParam(description = "The starting line number (1-indexed)")
        startLine: Int = 1,
        @McpToolParam(description = "The ending line number (inclusive)")
        endLine: Int? = null,
        @McpToolParam(description = "Maximum bytes to read to prevent memory issues")
        maxBytes: Int = 256 * 1024 * 1024 // 256MB
    ): FileContentResponse {
        val path: Path = this.resolveAndVerify(relativePath)
        require(path.isRegularFile()) { "File not found or not a regular file: $relativePath" }

        // Enforce SuperAdmin for sensitive files
        if (isSensitive(path) && !isSuperAdmin()) {
            throw SecurityException("Access to sensitive file '$relativePath' is restricted to SuperAdmins.")
        }

        require(!this.looksLikeBinaryFile(path)) { "File appears to be binary. Binary files are not supported." }

        val lines: List<String> = path.useLines { it.toList() }
        val totalLines: Int = lines.size
        val effectiveEndLine: Int = endLine ?: totalLines

        val startingIndex: Int = (startLine - 1).coerceAtLeast(0)
        val endingIndex: Int = (effectiveEndLine - startLine + 1).coerceAtLeast(0)
        val selectedLines: List<String> = lines.drop(startingIndex).take(endingIndex)

        val content: String = selectedLines.joinToString("\n")
        val bytes: ByteArray = content.toByteArray(StandardCharsets.UTF_8)

        val isTruncated: Boolean = bytes.size > maxBytes
        val finalContent: String = if (isTruncated) {
            this.getStringFromBytes(bytes, maxBytes)
        } else {
            content
        }

        return FileContentResponse(
            path = relativePath,
            totalLines = totalLines,
            content = finalContent,
            isTruncated = isTruncated
        )
    }

    @Tool(description = "List directory contents.")
    fun listDirectories(
        @McpToolParam(description = "The relative path of the directory to list")
        relativePath: String = ".",
        @McpToolParam(description = "Whether to list contents recursively")
        recursive: Boolean = false
    ): DirectoryListingResponse {
        val root: Path = this.resolveAndVerify(relativePath)
        require(root.isDirectory()) { "Not a directory: $relativePath" }

        val entries: MutableList<FileMetadata> = mutableListOf()
        val ignoredNames: Set<String> = properties.ignoredDirectories

        if (recursive) {
            Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (dir != root && ignoredNames.contains(dir.fileName.toString())) {
                        return FileVisitResult.SKIP_SUBTREE
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    entries.add(createMetadata(file))
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    if (dir != root) {
                        entries.add(createMetadata(dir))
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        } else {
            Files.newDirectoryStream(root).use { stream ->
                for (path in stream) {
                    if (!ignoredNames.contains(path.fileName.toString())) {
                        entries.add(createMetadata(path))
                    }
                }
            }
        }

        return DirectoryListingResponse(relativePath, entries)
    }

    @Tool(description = "Search for text within files in the project. Example: searchInFiles(\"TODO\", \"**/*.kt\")")
    @UserRestricted
    fun searchTextInFiles(
        @McpToolParam(description = "The text to search for within file contents")
        query: String,
        @McpToolParam(description = "The glob pattern to filter files (e.g., \"**/*.kt\")")
        glob: String = "**/*",
        @McpToolParam(description = "Number of surrounding lines to show for each match (default = 0)")
        padding: Int = 0
    ): FileSearchResponse {
        val results: MutableList<FileSearchResult> = mutableListOf()
        val ignoredNames: Set<String> = properties.ignoredDirectories
        val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:$glob")
        val isSuperAdmin: Boolean = this.isSuperAdmin()

        Files.walkFileTree(this.allowedRoot, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (dir != allowedRoot && ignoredNames.contains(dir.fileName.toString())) {
                    return FileVisitResult.SKIP_SUBTREE
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val relPath: Path = allowedRoot.relativize(file)
                if (pathMatcher.matches(relPath) && !isShadowedJavaScript(file) && !looksLikeBinaryFile(file)) {
                    if (isSensitive(file) && !isSuperAdmin) {
                        return FileVisitResult.CONTINUE
                    }
                    try {
                        val lines: List<String> = file.useLines { it.toList() }
                        val matchingLineIndices = lines.indices.filter { i -> lines[i].contains(query, ignoreCase = true) }

                        if (matchingLineIndices.isNotEmpty()) {
                            val contextBlocks = mutableListOf<String>()
                            if (padding <= 0) {
                                for (i in matchingLineIndices) {
                                    contextBlocks.add("Line ${i + 1}: ${lines[i]}")
                                }
                            } else {
                                // Merge ranges for padding
                                val ranges = matchingLineIndices.map { (it - padding).coerceAtLeast(0)..(it + padding).coerceAtMost(lines.size - 1) }
                                val mergedRanges = mutableListOf<IntRange>()
                                if (ranges.isNotEmpty()) {
                                    var current = ranges[0]
                                    for (i in 1 until ranges.size) {
                                        val next = ranges[i]
                                        if (next.first <= current.last + 1) {
                                            current = current.first..maxOf(current.last, next.last)
                                        } else {
                                            mergedRanges.add(current)
                                            current = next
                                        }
                                    }
                                    mergedRanges.add(current)
                                }

                                for (range in mergedRanges) {
                                    val block = range.joinToString("\n") { i -> "Line ${i + 1}: ${lines[i]}" }
                                    contextBlocks.add(block)
                                }
                            }
                            results.add(FileSearchResult(relPath.toString(), contextBlocks))
                        }
                    } catch (_: Exception) {
                        // Skip files that can't be read
                    }
                }
                return FileVisitResult.CONTINUE
            }
        })

        return FileSearchResponse(results)
    }

    @Tool(description = "Search for files by name or glob pattern within the project. Example: searchFiles(\"Toolkit\", \"*.kt\")")
    fun searchFiles(
        @McpToolParam(description = "The text to search for in the file name")
        query: String,
        @McpToolParam(description = "The glob pattern to filter files (e.g., \"**/*.kt\")")
        glob: String = "**/*"
    ): List<FileMetadata> {
        val results: MutableList<FileMetadata> = mutableListOf()
        val ignoredNames: Set<String> = properties.ignoredDirectories

        val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:$glob")

        Files.walkFileTree(this.allowedRoot, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (dir != allowedRoot && ignoredNames.contains(dir.fileName.toString())) {
                    return FileVisitResult.SKIP_SUBTREE
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                val relPath: Path = allowedRoot.relativize(file)
                if (pathMatcher.matches(relPath) && file.fileName.toString().contains(query, ignoreCase = true)
                    && !isShadowedJavaScript(file)) {
                    results.add(createMetadata(file))
                }
                return FileVisitResult.CONTINUE
            }
        })

        return results
    }

    @Tool(description = "Read multiple files at once.")
    @UserRestricted
    fun readManyFiles(
        @McpToolParam(description = "A list of relative paths to the files")
        paths: List<String>
    ): Map<String, FileContentResponse> {
        return paths.associateWith { path ->
            try {
                this.readFile(path)
            } catch (exception: SecurityException) {
                throw exception
            } catch (_: Exception) {
                throw IllegalArgumentException("Error reading file: $path")
            }
        }
    }

    @Tool(description = "Resolve a path hint using fuzzy matching if it doesn't exist.")
    fun resolvePath(
        @McpToolParam(description = "The path hint or partial filename to resolve")
        hint: String
    ): List<String> {
        val resolved: Path = this.allowedRoot.resolve(hint).normalize()
        val ignoredNames: Set<String> = this.properties.ignoredDirectories

        if (resolved.exists() && resolved.startsWith(this.allowedRoot)) {
            val relPath: Path = this.allowedRoot.relativize(resolved)
            if (!relPath.any { ignoredNames.contains(it.toString()) }) {
                return listOf(relPath.toString())
            }
        }

        val distanceCalculator = Levenshtein()
        val results: MutableList<Pair<String, Double>> = mutableListOf()

        Files.walkFileTree(this.allowedRoot, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (dir != allowedRoot && ignoredNames.contains(dir.fileName.toString())) {
                    return FileVisitResult.SKIP_SUBTREE
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                this.processPath(file)
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                if (dir != allowedRoot) {
                    this.processPath(dir)
                }
                return FileVisitResult.CONTINUE
            }

            private fun processPath(path: Path) {
                val relPath: String = allowedRoot.relativize(path).toString().replace("\\", "/")
                val fileName: String = path.fileName.toString()
                val score: Double = minOf(
                    distanceCalculator.distance(hint.lowercase(), relPath.lowercase()),
                    distanceCalculator.distance(hint.lowercase(), fileName.lowercase())
                )
                if (score < maxOf(hint.length, relPath.length) * 0.7) {
                    results.add(relPath to score)
                }
            }
        })

        return results
            .sortedBy { it.second }
            .take(5)
            .map { it.first }
    }

    private fun isShadowedJavaScript(path: Path): Boolean {
        val fileName: String = path.fileName.toString()

        if (!fileName.endsWith(".js")) {
            return false
        }
        val baseName: String = fileName.removeSuffix(".js")

        return listOf(".ts", ".tsx").any { extension ->
            val source: Path = path.resolveSibling(baseName + extension).normalize()
                 source.startsWith(this.allowedRoot) &&
                !source.fileName.toString().endsWith(".d.ts") &&
                 source.isRegularFile(LinkOption.NOFOLLOW_LINKS)
        }
    }

    internal fun resolveAndVerify(relativePath: String): Path {
        val resolvedPath: Path = allowedRoot.resolve(relativePath).normalize()
        require(resolvedPath.startsWith(allowedRoot)) { "Path is outside allowed root." }
        return resolvedPath
    }

    internal fun createMetadata(path: Path): FileMetadata {
        val attrs: BasicFileAttributes = path.readAttributes<BasicFileAttributes>()
        val lineCount: Int? = if (path.isRegularFile()) {
            try {
                path.useLines { it.count() }
            } catch (_: Exception) {
                null
            }
        } else null

        return FileMetadata(
            path = allowedRoot.relativize(path).toString(),
            size = attrs.size(),
            lastModified = attrs.lastModifiedTime().toString(),
            isDirectory = attrs.isDirectory,
            lineCount = lineCount?.toLong()
        )
    }
    internal fun looksLikeBinaryFile(path: Path): Boolean {
        val sample: ByteArray = path.inputStream().use { it.readNBytes(1024) }
        return sample.any { it == 0.toByte() }
    }

    internal fun getStringFromBytes(
        bytes: ByteArray,
        maxBytes: Int
    ): String {
        return String(
            bytes.take(maxBytes).toByteArray(),
            StandardCharsets.UTF_8
        )
    }

    // TODO: Move to service file.
    private fun isSensitive(path: Path): Boolean {
        val relPath = allowedRoot.relativize(path).toString()
        val matcher = FileSystems.getDefault()
        return properties.sensitiveFilePatterns.any { pattern ->
            matcher.getPathMatcher("glob:$pattern").matches(Paths.get(relPath))
        }
    }

    // TODO: Move to service file.
    private fun isSuperAdmin(): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.authorities?.any { it.authority == UserRoleTier.SUPER_ADMIN.authority } ?: false
    }
}
