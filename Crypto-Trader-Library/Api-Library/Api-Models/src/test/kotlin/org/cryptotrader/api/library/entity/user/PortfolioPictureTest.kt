package org.cryptotrader.api.library.entity.user

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Portfolio Picture")
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
    @DisplayName("Get File Type")
    inner class GetFileType {
        @Test
        @DisplayName("Should throw exceptions with invalid file types")
        fun getFileType_ThrowsException_WithInvalidFileType() {
            val invalidPlaceholder = ".file"
            fileNamePlaceholder = invalidPlaceholder
            assertThrows(IllegalArgumentException::class.java) {
                ProfilePicture.getFileType(fileNamePlaceholder)
            }
        }

        @Test
        @DisplayName("Should not throw exceptions with valid file types")
        fun getFileType_DoesNotThrowException_WithValidFileType() {
            val validPlaceholder = ".jpg"
            fileNamePlaceholder = validPlaceholder
            assertDoesNotThrow {
                ProfilePicture.getFileType(fileNamePlaceholder)
            }
        }

        @Test
        @DisplayName("Identifies file types correctly with valid file types")
        fun getFileType_IdentifiesFileTypes_ValidFileTypes() {
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