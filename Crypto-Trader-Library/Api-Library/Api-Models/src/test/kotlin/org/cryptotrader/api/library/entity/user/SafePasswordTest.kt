package org.cryptotrader.api.library.entity.user

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("SafePassword")
@Tag("entity")
@DisplayName("Safe Password")
class SafePasswordTest : CryptoTraderTest() {

    private lateinit var safePassword: SafePassword
    private val testUnencodedPassword = "unencoded-password"

    @BeforeEach
    fun setUp() {
        this.safePassword = SafePassword()
    }

    @Nested
    @Tag("constructor")
    @DisplayName("Constructors")
    inner class Constructors {
        @Test
        @DisplayName("Should construct with no-args")
        fun constructor_Constructs_NoArgs() {
            safePassword = SafePassword()
            assertNotNull { safePassword }
            assertNotNull { safePassword.encodedPassword }
            assertNotNull { safePassword.encoder }

        }

        @Test
        @DisplayName("Should construct from unencoded password")
        fun constructor_Constructs_FromUnencoded() { }
    }

    @Nested
    @Tag("encodePassword")
    @DisplayName("Encode Password")
    inner class EncodePassword {
        @Test
        @DisplayName("Should encode a valid raw password")
        fun encodePassword_Encodes_ValidRawPassword() {
            val encodedPassword = safePassword.encodePassword(testUnencodedPassword)
            assertNotEquals(testUnencodedPassword, encodedPassword)
        }
    }

    @Nested
    @Tag("compareUnencodedPassword")
    @DisplayName("Compare Unencoded Password")
    inner class CompareUnencodedPassword {
        @Test
        @DisplayName("Should match corresponding raw password to encoded")
        fun compareUnencodedPassword_Matches_RawPasswordToEncoded() {
            val encodedPassword = safePassword.encodePassword(testUnencodedPassword)
            assertNotEquals(testUnencodedPassword, encodedPassword)
            val matches = safePassword.compareUnencodedPassword(testUnencodedPassword)
            assertTrue(matches)
        }
    }
}
