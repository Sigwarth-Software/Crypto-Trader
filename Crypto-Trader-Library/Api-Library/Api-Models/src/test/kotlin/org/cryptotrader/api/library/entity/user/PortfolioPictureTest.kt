package org.cryptotrader.api.library.entity.user

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ProfilePicture")
@UnitSuite
class PortfolioPictureTest : CryptoTraderTest() {
    private lateinit var testProfilePicture: ProfilePicture
    private lateinit var fileNamePlaceholder: String
    private lateinit var testBytes: ByteArray

    @BeforeEach
    fun setUp() {
        this.testBytes = ByteArray(10)
        this.testProfilePicture = ProfilePicture("placeholder.jpg",this.testBytes)
    }

    @Nested
    @DisplayName("getFileType")
    inner class GetFileType {
        @Nested
        @DisplayName("when the file type is invalid")
        inner class WhenTheFileTypeIsInvalid {
            @BeforeEach
            fun setUp() {
                val invalidPlaceholder = ".file"
                fileNamePlaceholder = invalidPlaceholder
            }

            @Test
            @DisplayName("should throw exceptions with invalid file types")
            fun throwExceptionsWithInvalidFileTypes() {
                assertThrows(IllegalArgumentException::class.java) {
                    ProfilePicture.getFileType(fileNamePlaceholder)
                }
            }
        }

        @Nested
        @DisplayName("when the file extension is jpg")
        inner class WhenTheFileExtensionIsJpg {
            @BeforeEach
            fun setUp() {
                val validPlaceholder = ".jpg"
                fileNamePlaceholder = validPlaceholder
            }

            @Test
            @DisplayName("should not throw exceptions with valid file types")
            fun notThrowExceptionsWithValidFileTypes() {
                assertDoesNotThrow {
                    ProfilePicture.getFileType(fileNamePlaceholder)
                }
            }
        }

        @Nested
        @DisplayName("when the file type is supported")
        inner class WhenTheFileTypeIsSupported {
            @Test
            @DisplayName("should identify file types correctly with valid file types")
            fun identifyFileTypesCorrectlyWithValidFileTypes() {
                val validFileTypes = arrayOf("jpg", "jpeg", "gif", "bmp", "webp", "svg")
                for (validFileType in validFileTypes) {
                    fileNamePlaceholder = "validFileType.$validFileType"
                    var expectedFileType = "image/$validFileType"
                    if (validFileType == "jpg" || validFileType == "jpeg") {
                        expectedFileType = "image/jpeg"
                    }
                    if (validFileType == "svg") {
                        expectedFileType = "image/svg+xml"
                    }
                    val actualFileType = ProfilePicture.getFileType(fileNamePlaceholder)
                    assertEquals(expectedFileType, actualFileType)
                }
            }
        }
    }
}
