package org.cryptotrader.api.library.entity.user

import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SafePassword")
@UnitSuite
class SafePasswordTest : CryptoTraderTest() {

    private lateinit var safePassword: SafePassword
    private val testUnencodedPassword = "unencoded-password"

    @BeforeEach
    fun setUp() {
        this.safePassword = SafePassword()
    }

    @Nested
    @DisplayName("constructor")
    inner class Constructors {
        @Test
        @DisplayName("should construct with no-args")
        fun constructWithNoArgs() {
            safePassword = SafePassword()
            assertNotNull { safePassword }
            assertNotNull { safePassword.encodedPassword }
            assertNotNull { safePassword.encoder }
        }
    }

    @Nested
    @DisplayName("encodePassword")
    inner class EncodePassword {
        @Test
        @DisplayName("should encode a valid raw password")
        fun encodeAValidRawPassword() {
            val encodedPassword = safePassword.encodePassword(testUnencodedPassword)
            assertNotEquals(testUnencodedPassword, encodedPassword)
        }
    }

    @Nested
    @DisplayName("compareUnencodedPassword")
    inner class CompareUnencodedPassword {
        @Test
        @DisplayName("should match corresponding raw password to encoded")
        fun matchCorrespondingRawPasswordToEncoded() {
            val encodedPassword = safePassword.encodePassword(testUnencodedPassword)
            assertNotEquals(testUnencodedPassword, encodedPassword)
            val matches = safePassword.compareUnencodedPassword(testUnencodedPassword)
            assertTrue(matches)
        }
    }
}
