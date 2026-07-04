package org.cryptotrader.version.script

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.version.library.model.element.PomElement
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.Path

@DisplayName("Pom Parser")
class PomParserTest : CryptoTraderTest() {
    lateinit var testPath: Path
    @BeforeEach
    fun setUp() {
        val userDirectory = System.getProperty("user.dir")
        this.testPath = Path("$userDirectory/pom.xml")
    }

    @Nested
    @DisplayName("Get Pom")
    inner class GetPom {
        @Test
        @DisplayName("Should get pom elements from valid paths")
        fun getPom_GetsPomElements_ValidPath() {
            assertNotNull(testPath, "Test path is null")
            val pomElement: PomElement = PomParser.getPom(testPath)
            assertNotNull(pomElement)
        }

        @Test
        @DisplayName("Should throw exception when given invalid path")
        fun getPom_ThrowsException_InvalidPath() {
            val nonPomFile = System.getProperty("user.dir") + "/README.md"
            testPath = Path(nonPomFile)
            assertThrows(RuntimeException::class.java) {
                PomParser.getPom(testPath)
            }
        }
    }
}