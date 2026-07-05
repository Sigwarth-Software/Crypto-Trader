package org.cryptotrader.security.library.service

import inet.ipaddr.IPAddressString
import org.cryptotrader.security.library.model.BanType
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("In-Memory IP Ban Service")
class InMemoryIpBanServiceTest : CryptoTraderTest() {
    private lateinit var ipBanService: InMemoryIpBanService

    @BeforeEach
    fun setUp() {
        this.ipBanService = InMemoryIpBanService()
    }

    @Nested
    @DisplayName("Blocked Addresses")
    inner class BlockedAddresses {
        @Test
        @DisplayName("Should store banned IPs")
        fun blockedAddresses_StoresBannedIPs() {
            val testIp = "0.0.0.0"
            ipBanService.blockedAddresses[testIp] = IPAddressString(testIp)
            assertTrue(ipBanService.blockedAddresses.containsKey(testIp))
        }
    }

    @Nested
    @DisplayName("Is Banned")
    inner class IsBanned {
        @Test
        @DisplayName("Should detect blocked IPs")
        fun isBanned_DetectsBannedIPs() {
            val testIp = "0.0.0.0"
            ipBanService.blockedAddresses[testIp] = IPAddressString(testIp)
            assertTrue(ipBanService.isBanned(testIp))
        }
    }

    @Nested
    @DisplayName("Ban")
    inner class Ban {
        @Test
        @DisplayName("Should ban IPs")
        fun ban_BansIPs() {
            val testIp = "0.0.0.0"
            ipBanService.ban(testIp, BanType.TEMP)
            assertTrue(ipBanService.isBanned(testIp))
        }

        @Test
        @DisplayName("Should ignore permanent bans")
        fun ban_IgnoresPermanentBans() {
            val testIp = "0.0.0.0"
            ipBanService.ban(testIp, BanType.PERMA)
            assertFalse(ipBanService.isBanned(testIp))
        }
    }

    @Nested
    @DisplayName("Unban")
    inner class Unban {
        @Test
        @DisplayName("Should unban IPs that are banned")
        fun unban_UnbansIps_WhenBanned() {
            val testIp = "0.0.0.0"
            ipBanService.ban(testIp, BanType.TEMP)
            assertTrue(ipBanService.isBanned(testIp))
            ipBanService.unban(testIp, BanType.TEMP)
            assertFalse(ipBanService.isBanned(testIp))
        }
    }
}
