package org.cryptotrader.agent.library.component

import org.cryptotrader.agent.library.communication.response.DirectoryListingResponse
import org.cryptotrader.agent.library.communication.response.FileContentResponse
import org.cryptotrader.agent.library.config.AgentConstraintsProperties
import org.cryptotrader.agent.library.model.FileMetadata
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.writeText

@DisplayName("FileReaderTool")
@Tag("FileReaderTool")
@UnitSuite
class FileReaderToolTest : CryptoTraderTest() {

    @TempDir
    lateinit var tempDir: Path

    @Mock
    lateinit var properties: AgentConstraintsProperties

    @InjectMocks
    lateinit var fileReaderTool: FileReaderTool

    val mockKotlinSource = "src/main/kotlin"
    val mockKotlinSourceFile = "$mockKotlinSource/Tool.kt"
    val mockKotlinTestFile = "$mockKotlinSource/Test.kt"
    val mockReadMeFile = "README.md"
    val ignoredDir = "ignored_dir"
    val ignoredDirFile = "$ignoredDir/Match.kt"
    val allowedDir = "allowed_dir"
    val allowedDirFile = "$allowedDir/Match.kt"

    @BeforeEach
    fun setUp() {
        val absoluteDirectoryPath: String = this.tempDir.toAbsolutePath().toString()
        `when`(properties.allowedRoot).thenReturn(absoluteDirectoryPath)
        this.fileReaderTool = FileReaderTool(this.properties)
    }

    @Nested
    @DisplayName("readFile")
    @Tag("readFile")
    inner class ReadFile {
        lateinit var testFile: Path

        @Test
        @DisplayName("should throw error for file outside root")
        fun throwErrorForFileOutsideRoot() {
            assertThrows(IllegalArgumentException::class.java) {
                fileReaderTool.readFile("../outside.txt")
            }
        }


        @Nested
        @DisplayName("when the file is text")
        inner class WhenFileIsText {
            val mockTextFileName: String = "test.txt"

            @BeforeEach
            fun setUp() {
                testFile = tempDir.resolve(mockTextFileName)
            }

            @Test
            @DisplayName("should read content of a text file")
            fun readContentOfTextFile() {
                val content = "Line 1\nLine 2\nLine 3"
                testFile.writeText(content)

                val response: FileContentResponse = fileReaderTool.readFile(mockTextFileName)

                assertEquals(mockTextFileName, response.path)
                assertEquals(3, response.totalLines)
                assertEquals(content, response.content)
                assertFalse(response.isTruncated)
            }

            @Test
            @DisplayName("should respect start and end lines")
            fun respectStartAndEndLines() {
                val content = "Line 1\nLine 2\nLine 3\nLine 4"
                testFile.writeText(content)

                val response: FileContentResponse = fileReaderTool.readFile(
                    mockTextFileName, 2, 3
                )

                assertEquals(4, response.totalLines)
                assertEquals("Line 2\nLine 3", response.content)
            }
        }

        @Nested
        @DisplayName("when the file is binary")
        inner class WhenFileIsBinary {
            val mockBinaryFileName = "binary.dat"
            lateinit var testFile: Path

            @BeforeEach
            fun setup() {
                this.testFile = tempDir.resolve(this.mockBinaryFileName)
                this.testFile.toFile().writeBytes(byteArrayOf(0, 1, 2, 3))
            }

            @Test
            @DisplayName("should throw error")
            fun throwErrorForBinaryFile() {
                val exception: IllegalArgumentException? =
                    assertThrows(IllegalArgumentException::class.java) {
                        fileReaderTool.readFile(this.mockBinaryFileName)
                    }

                if (exception == null) {
                    fail("Expected an IllegalArgumentException to be thrown")
                }
                assertTrue(
                    exception.message!!.contains(
                        "binary files are not supported",
                        ignoreCase = true
                    )
                )
            }
        }


        @Nested
        @DisplayName("when attempting super-admin privileged access as a non-super-admin")
        inner class WhenAttemptingSuperAdminAccess {
            lateinit var sensitiveFile: Path
            @BeforeEach
            fun setUp() {
                `when`(properties.sensitiveFilePatterns).thenReturn(setOf("*.sensitive"))
                sensitiveFile = tempDir.resolve("secret.sensitive")
                sensitiveFile.writeText("top secret content")
            }

            @Test
            @DisplayName("should throw security exception")
            fun throwSecurityException() {
                assertThrows(SecurityException::class.java) {
                    fileReaderTool.readFile(sensitiveFile.toString())
                }
            }
        }
    }

    @Nested
    @DisplayName("listDirectories")
    inner class ListDirectories {
        val mockEmptyDirectory = "dir1"
        val mockFile = "file1.txt"
        val mockNonEmptyDirectory = "dir1/file2.txt"

        @Nested
        @DisplayName("when discovering valid files")
        inner class WhenDiscoveringValidFiles {
            @BeforeEach
            fun setUp() {
                tempDir.resolve(mockEmptyDirectory).createDirectories()
                tempDir.resolve(mockFile).writeText("hello")
                tempDir.resolve(mockNonEmptyDirectory).writeText("world")
            }

            @Test
            @DisplayName("should list files in the directory")
            fun listFilesInDirectory() {
                val response: DirectoryListingResponse =
                    fileReaderTool.listDirectories(".", recursive = false)

                assertEquals(".", response.path)
                assertEquals(2, response.entries.size)
                assertTrue(response.entries.any { it.path == mockFile })
                assertTrue(response.entries.any { it.path == mockEmptyDirectory })
            }

            @Test
            @DisplayName("should list files recursively")
            fun listFilesRecursively() {
                val response: DirectoryListingResponse =
                    fileReaderTool.listDirectories(".", recursive = true)

                assertEquals(3, response.entries.size)
                val paths: List<String> = response.entries.map {
                    it.path.replace("\\", "/")
                }
                assertTrue(paths.contains(mockNonEmptyDirectory))
            }
        }

        @Nested
        @DisplayName("when discovering invalid files")
        inner class WhenDiscoveringInvalidFiles {
            @BeforeEach
            fun setUp() {
                `when`(properties.ignoredDirectories).thenReturn(
                    setOf(ignoredDir)
                )
            }

            @Test
            @DisplayName("should ignore specified directories")
            fun ignoreSpecifiedDirectories() {
                tempDir.resolve(ignoredDir).createDirectories()
                tempDir.resolve(ignoredDirFile).createFile()
                tempDir.resolve(allowedDir).createDirectories()
                tempDir.resolve(allowedDirFile).createFile()

                val response =
                    fileReaderTool.listDirectories(".", recursive = true)

                assertFalse(response.entries.any {
                    it.path.contains(ignoredDir)
                })
                assertTrue(response.entries.any {
                    it.path.contains(allowedDir)
                })
            }
        }
    }

    @Nested
    @DisplayName("searchFiles")
    inner class SearchFiles {
        @BeforeEach
        fun setUp() {
            tempDir.resolve(mockKotlinSource).createDirectories()
            tempDir.resolve(mockKotlinSource).createDirectories()
            tempDir.resolve(mockKotlinSourceFile).writeText("content")
            tempDir.resolve(mockKotlinTestFile).writeText("content")
            tempDir.resolve(mockReadMeFile).writeText("content")

            tempDir.resolve(ignoredDir).createDirectories()
            tempDir.resolve(ignoredDirFile).writeText("content")
            tempDir.resolve(allowedDir).createDirectories()
            tempDir.resolve(allowedDirFile).writeText("content")

            `when`(properties.ignoredDirectories).thenReturn(
                setOf(ignoredDir)
            )
        }

        @Test
        @DisplayName("should find files by query and glob")
        fun findFilesByQueryAndGlob() {
            val results: List<FileMetadata> = fileReaderTool.searchFiles(
                "Tool", "**/*.kt"
            )

            assertEquals(1, results.size)
            val actual: String = results[0].path.replace("/", File.separator)
            assertEquals(mockKotlinSourceFile.replace("/", File.separator),
                actual
            )
        }

        @Test
        @DisplayName("should ignore files in ignored directories")
        fun ignoreFilesInIgnoredDirectories() {
            val results: List<FileMetadata> = fileReaderTool.searchFiles(
                "Match", "**/*.kt"
            )

            assertEquals(1, results.size)
            assertTrue(results[0].path.contains(allowedDir))
            assertFalse(results[0].path.contains(ignoredDir))
        }
    }

    @Nested
    @DisplayName("resolvePath")
    inner class ResolvePath {
        val targetFileName: String = "very-long-filename-that-is-unique.txt"

        @Test
        @DisplayName("should fuzzy match existing files")
        fun fuzzyMatchExistingFiles() {
            tempDir.resolve(this.targetFileName).writeText("content")

            val exactMatch: List<String> = fileReaderTool.resolvePath(
                this.targetFileName
            )
            assertEquals(listOf(this.targetFileName), exactMatch)

            val fuzzyMatch: List<String> = fileReaderTool.resolvePath("very-long-unique")
            assertTrue(fuzzyMatch.contains(this.targetFileName))
        }

        @Nested
        @DisplayName("when in ignored directories")
        inner class WhenInIgnoredDirectories {
            val fileToIgnore: String = "very-unique-file.txt"

            @BeforeEach
            fun setUp() {
                `when`(properties.ignoredDirectories).thenReturn(
                    setOf(ignoredDir)
                )
            }

            @Test
            @DisplayName("should ignore files in ignored directories")
            fun ignoreFilesInIgnoredDirectories() {
                tempDir.resolve(ignoredDir).createDirectories()
                tempDir.resolve("$ignoredDir/${this.fileToIgnore}")
                    .writeText("content")
                tempDir.resolve(allowedDir).createDirectories()
                tempDir.resolve("$allowedDir/normal-file.txt")
                    .writeText("content")

                val results: List<String> = fileReaderTool.resolvePath(
                    "very-unique-file"
                )

                assertFalse(
                    results.contains("$ignoredDir/${this.fileToIgnore}"),
                    "Results should not contain file from ignored directory"
                )
            }
        }
    }
}
