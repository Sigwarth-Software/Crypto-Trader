package org.cryptotrader.security.library.service

import inet.ipaddr.IPAddressString
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.cryptotrader.security.library.model.BanType
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("InMemoryIpBanService")
@UnitSuite
class InMemoryIpBanServiceTest : CryptoTraderTest() {
    private lateinit var ipBanService: InMemoryIpBanService

    @BeforeEach
    fun setUp() {
        this.ipBanService = InMemoryIpBanService()
    }

    @Nested
    @DisplayName("blockedAddresses")
    inner class BlockedAddresses {
        @Test
        @DisplayName("should store banned IPs")
        fun storeBannedIPs() {
            val testIp = "0.0.0.0"
            ipBanService.blockedAddresses[testIp] = IPAddressString(testIp)
            assertTrue(ipBanService.blockedAddresses.containsKey(testIp))
        }
    }

    @Nested
    @DisplayName("isBanned")
    inner class IsBanned {
        @Nested
        @DisplayName("when the IP address is blocked")
        inner class WhenTheIPAddressIsBlocked {
            private lateinit var testIp: String

            @BeforeEach
            fun setUp() {
                testIp = "0.0.0.0"
                ipBanService.blockedAddresses[testIp] = IPAddressString(testIp)
            }

            @Test
            @DisplayName("should detect blocked IPs")
            fun detectBlockedIPs() {
                assertTrue(ipBanService.isBanned(testIp))
            }
        }
    }

    @Nested
    @DisplayName("ban")
    inner class Ban {
        @Nested
        @DisplayName("when the ban is temporary")
        inner class WhenTheBanIsTemporary {
            private lateinit var testIp: String

            @BeforeEach
            fun setUp() {
                testIp = "0.0.0.0"
            }

            @Test
            @DisplayName("should ban IPs")
            fun banIPs() {
                ipBanService.ban(testIp, BanType.TEMP)
                assertTrue(ipBanService.isBanned(testIp))
            }
        }

        @Nested
        @DisplayName("when the ban is permanent")
        inner class WhenTheBanIsPermanent {
            private lateinit var testIp: String

            @BeforeEach
            fun setUp() {
                testIp = "0.0.0.0"
            }

            @Test
            @DisplayName("should ignore permanent bans")
            fun ignorePermanentBans() {
                ipBanService.ban(testIp, BanType.PERMA)
                assertFalse(ipBanService.isBanned(testIp))
            }
        }
    }

    @Nested
    @DisplayName("unban")
    inner class Unban {
        @Nested
        @DisplayName("when the IP address is banned")
        inner class WhenBanned {
            private lateinit var testIp: String

            @BeforeEach
            fun setUp() {
                testIp = "0.0.0.0"
                ipBanService.ban(testIp, BanType.TEMP)
            }

            @Test
            @DisplayName("should unban IPs that are banned")
            fun unbanIPsThatAreBanned() {
                assertTrue(ipBanService.isBanned(testIp))
                ipBanService.unban(testIp, BanType.TEMP)
                assertFalse(ipBanService.isBanned(testIp))
            }
        }
    }
}
