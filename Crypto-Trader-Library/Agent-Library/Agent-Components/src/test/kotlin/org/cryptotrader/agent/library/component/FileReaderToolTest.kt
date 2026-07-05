package org.cryptotrader.agent.library.component

import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.Mockito.`when`
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.writeText

class FileReaderToolTest : CryptoTraderTest() {

    @TempDir
    lateinit var tempDir: Path

    @Mock
    lateinit var properties: AgentConstraintsProperties

    lateinit var fileReaderTool: FileReaderTool

    @BeforeEach
    fun setUp() {
        `when`(properties.allowedRoot).thenReturn(tempDir.toAbsolutePath().toString())
        fileReaderTool = FileReaderTool(properties)
    }

    @Nested
    @DisplayName("Read File")
    inner class ReadFile {
        @Test
        @DisplayName("readFile should read content of a text file")
        fun readFile_ReadsTextFile() {
            val testFile = tempDir.resolve("test.txt")
            val content = "Line 1\nLine 2\nLine 3"
            testFile.writeText(content)

            val response = fileReaderTool.readFile("test.txt")

            assertEquals("test.txt", response.path)
            assertEquals(3, response.totalLines)
            assertEquals(content, response.content)
            assertFalse(response.isTruncated)
        }

        @Test
        @DisplayName("readFile should respect startLine and endLine")
        fun readFile_RespectsLines() {
            val testFile = tempDir.resolve("test.txt")
            val content = "Line 1\nLine 2\nLine 3\nLine 4"
            testFile.writeText(content)

            val response = fileReaderTool.readFile("test.txt", startLine = 2, endLine = 3)

            assertEquals(4, response.totalLines)
            assertEquals("Line 2\nLine 3", response.content)
        }

        @Test
        @DisplayName("readFile should throw error for binary file")
        fun readFile_ThrowsOnBinary() {
            val testFile = tempDir.resolve("binary.dat")
            testFile.toFile().writeBytes(byteArrayOf(0, 1, 2, 3))

            val exception = assertThrows(IllegalArgumentException::class.java) {
                fileReaderTool.readFile("binary.dat")
            }
            assertTrue(exception.message!!.contains("binary files are not supported", ignoreCase = true))
        }

        @Test
        @DisplayName("readFile should throw error for file outside root")
        fun readFile_ThrowsOnOutsideRoot() {
            assertThrows(IllegalArgumentException::class.java) {
                fileReaderTool.readFile("../outside.txt")
            }
        }
    }

    @Nested
    @DisplayName("List Directories")
    inner class ListDirectories {
        @Test
        @DisplayName("listDirectory should list files in directory")
        fun listDirectories_ListsFiles() {
            tempDir.resolve("dir1").createDirectories()
            tempDir.resolve("file1.txt").writeText("hello")
            tempDir.resolve("dir1/file2.txt").writeText("world")

            val response = fileReaderTool.listDirectories(".", recursive = false)

            assertEquals(".", response.path)
            assertEquals(2, response.entries.size)
            assertTrue(response.entries.any { it.path == "file1.txt" })
            assertTrue(response.entries.any { it.path == "dir1" })
        }

        @Test
        @DisplayName("listDirectory should list files recursively")
        fun listDirectories_ListsRecursively() {
            tempDir.resolve("dir1").createDirectories()
            tempDir.resolve("file1.txt").writeText("hello")
            tempDir.resolve("dir1/file2.txt").writeText("world")

            val response = fileReaderTool.listDirectories(".", recursive = true)

            assertEquals(3, response.entries.size)
            val paths = response.entries.map { it.path.replace("\\", "/") }
            assertTrue(paths.contains("dir1/file2.txt"))
        }

        @Test
        @DisplayName("listDirectories should ignore specified directories")
        fun listDirectories_IgnoresSpecified() {
            `when`(properties.ignoredDirectories).thenReturn(setOf("ignored_dir"))
            tempDir.resolve("ignored_dir").createDirectories()
            tempDir.resolve("ignored_dir/file.txt").createFile()
            tempDir.resolve("allowed_dir").createDirectories()
            tempDir.resolve("allowed_dir/file.txt").createFile()

            val response = fileReaderTool.listDirectories(".", recursive = true)

            assertFalse(response.entries.any { it.path.contains("ignored_dir") })
            assertTrue(response.entries.any { it.path.contains("allowed_dir") })
        }
    }

    @Nested
    @DisplayName("Search Files")
    inner class SearchFiles {
        @Test
        @DisplayName("searchFiles should find files by query and glob")
        fun searchFiles_FindsByQueryAndGlob() {
            tempDir.resolve("src/main/kotlin").createDirectories()
            tempDir.resolve("src/test/kotlin").createDirectories()
            tempDir.resolve("src/main/kotlin/Tool.kt").writeText("content")
            tempDir.resolve("src/test/kotlin/Test.kt").writeText("content")
            tempDir.resolve("README.md").writeText("content")

            val results = fileReaderTool.searchFiles("Tool", glob = "**/*.kt")

            assertEquals(1, results.size)
            assertEquals("src/main/kotlin/Tool.kt".replace("/", File.separator), results[0].path.replace("/", File.separator))
        }

        @Test
        @DisplayName("searchFiles should ignore files in ignored directories")
        fun searchFiles_IgnoresIgnored() {
            `when`(properties.ignoredDirectories).thenReturn(setOf("ignored_dir"))
            tempDir.resolve("ignored_dir").createDirectories()
            tempDir.resolve("ignored_dir/Match.kt").writeText("content")
            tempDir.resolve("allowed_dir").createDirectories()
            tempDir.resolve("allowed_dir/Match.kt").writeText("content")

            val results = fileReaderTool.searchFiles("Match", glob = "**/*.kt")

            assertEquals(1, results.size)
            assertTrue(results[0].path.contains("allowed_dir"))
            assertFalse(results[0].path.contains("ignored_dir"))
        }
    }

    @Nested
    @DisplayName("Resolve Path")
    inner class ResolvePath {
        @Test
        @DisplayName("resolvePath should fuzzy match existing files")
        fun resolvePath_FuzzyMatch() {
            tempDir.resolve("very-long-filename-that-is-unique.txt").writeText("content")

            // Exact match (normalized)
            val exactMatch = fileReaderTool.resolvePath("very-long-filename-that-is-unique.txt")
            assertEquals(listOf("very-long-filename-that-is-unique.txt"), exactMatch)

            // Fuzzy match
            val fuzzyMatch = fileReaderTool.resolvePath("very-long-unique")
            assertTrue(fuzzyMatch.contains("very-long-filename-that-is-unique.txt"))
        }

        @Test
        @DisplayName("resolvePath should ignore files in ignored directories")
        fun resolvePath_IgnoresIgnored() {
            `when`(properties.ignoredDirectories).thenReturn(setOf("ignored_dir"))
            tempDir.resolve("ignored_dir").createDirectories()
            tempDir.resolve("ignored_dir/very-unique-file.txt").writeText("content")
            tempDir.resolve("allowed_dir").createDirectories()
            tempDir.resolve("allowed_dir/normal-file.txt").writeText("content")

            val results = fileReaderTool.resolvePath("very-unique-file")

            assertFalse(results.contains("ignored_dir/very-unique-file.txt"), "Results should not contain file from ignored directory")
        }
    }
}
