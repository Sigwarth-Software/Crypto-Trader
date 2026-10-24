package org.cryptotrader.version.script

import java.nio.file.Path
import kotlin.io.path.Path
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.cryptotrader.version.library.model.element.PomElement
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("PomParser")
@UnitSuite
class PomParserTest : CryptoTraderTest() {
    lateinit var testPath: Path
    @BeforeEach
    fun setUp() {
        val userDirectory = System.getProperty("user.dir")
        this.testPath = Path("$userDirectory/pom.xml")
    }

    @Nested
    @DisplayName("getPom")
    inner class GetPom {
        @Nested
        @DisplayName("when the path points to a POM")
        inner class WhenThePathPointsToAPOM {
            @Test
            @DisplayName("should get pom elements from valid paths")
            fun getPomElementsFromValidPaths() {
                assertNotNull(testPath, "Test path is null")
                val pomElement: PomElement = PomParser.getPom(testPath)
                assertNotNull(pomElement)
            }
        }

        @Nested
        @DisplayName("when the path points to a README")
        inner class WhenThePathPointsToAREADME {
            @BeforeEach
            fun setUp() {
                val nonPomFile = System.getProperty("user.dir") + "/README.md"
                testPath = Path(nonPomFile)
            }

            @Test
            @DisplayName("should throw exception")
            fun throwException() {
                assertThrows(RuntimeException::class.java) {
                    PomParser.getPom(testPath)
                }
            }
        }
    }
}
