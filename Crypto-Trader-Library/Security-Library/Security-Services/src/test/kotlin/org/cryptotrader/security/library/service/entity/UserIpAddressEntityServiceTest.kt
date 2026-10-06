package org.cryptotrader.security.library.service.entity

import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.entity.ip.UserIpAddress
import org.cryptotrader.security.library.repository.UserIpAddressRepository
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.Optional

@DisplayName("User IP Address Entity Service")
class UserIpAddressEntityServiceTest : CryptoTraderTest() {
    private lateinit var repository: UserIpAddressRepository
    private lateinit var service: UserIpAddressEntityService

    private val testUser = ProductUser.builder().email("trader@cryptotrader.org").build().apply { id = 1L }
    private val testIp = "192.168.1.1"

    @BeforeEach
    fun setUp() {
        this.repository = mock(UserIpAddressRepository::class.java)
        this.service = UserIpAddressEntityService(this.repository)
    }

    @Nested
    @DisplayName("Record User IP")
    inner class RecordUserIp {

        @Test
        @DisplayName("Should create new UserIpAddress record when IP does not exist for user")
        fun recordUserIp_CreatesNew_WhenNotExists() {
            `when`(repository.findByUserAndIpAddress(testUser, testIp)).thenReturn(Optional.empty())
            val savedIp = UserIpAddress(testUser, testIp)
            `when`(repository.save(any(UserIpAddress::class.java))).thenReturn(savedIp)

            val result = service.recordUserIp(testUser, testIp)

            val captor = ArgumentCaptor.forClass(UserIpAddress::class.java)
            verify(repository, times(1)).save(captor.capture())
            assertThat(captor.value.user).isEqualTo(testUser)
            assertThat(captor.value.ipAddress).isEqualTo(testIp)
            assertThat(captor.value.lastAccessed).isNotNull
            assertThat(result).isNotNull
        }

        @Test
        @DisplayName("Should update lastAccessed when IP already exists for user")
        fun recordUserIp_UpdatesExisting_WhenExists() {
            val previousTime = LocalDateTime.now().minusDays(1)
            val existing = UserIpAddress(testUser, testIp, previousTime)

            `when`(repository.findByUserAndIpAddress(testUser, testIp)).thenReturn(Optional.of(existing))
            `when`(repository.save(any(UserIpAddress::class.java))).thenAnswer { it.getArgument(0) }

            val result = service.recordUserIp(testUser, testIp)

            verify(repository, times(1)).save(existing)
            assertThat(result.lastAccessed).isAfter(previousTime)
        }
    }

    @Nested
    @DisplayName("Queries")
    inner class Queries {

        @Test
        @DisplayName("Should find by user and IP address")
        fun findByUserAndIpAddress() {
            val expected = UserIpAddress(testUser, testIp)
            `when`(repository.findByUserAndIpAddress(testUser, testIp)).thenReturn(Optional.of(expected))

            val result = service.findByUserAndIpAddress(testUser, testIp)

            assertThat(result).isPresent
            assertThat(result.get()).isEqualTo(expected)
        }

        @Test
        @DisplayName("Should find by userId and IP address")
        fun findByUserIdAndIpAddress() {
            val expected = UserIpAddress(testUser, testIp)
            `when`(repository.findByUserIdAndIpAddress(1L, testIp)).thenReturn(Optional.of(expected))

            val result = service.findByUserIdAndIpAddress(1L, testIp)

            assertThat(result).isPresent
            assertThat(result.get()).isEqualTo(expected)
        }

        @Test
        @DisplayName("Should find all by user")
        fun findAllByUser() {
            val list = listOf(UserIpAddress(testUser, testIp))
            `when`(repository.findAllByUser(testUser)).thenReturn(list)

            val result = service.findAllByUser(testUser)

            assertThat(result).isEqualTo(list)
        }

        @Test
        @DisplayName("Should find all by userId")
        fun findAllByUserId() {
            val list = listOf(UserIpAddress(testUser, testIp))
            `when`(repository.findAllByUserId(1L)).thenReturn(list)

            val result = service.findAllByUserId(1L)

            assertThat(result).isEqualTo(list)
        }

        @Test
        @DisplayName("Should find all by IP address")
        fun findAllByIpAddress() {
            val list = listOf(UserIpAddress(testUser, testIp))
            `when`(repository.findAllByIpAddress(testIp)).thenReturn(list)

            val result = service.findAllByIpAddress(testIp)

            assertThat(result).isEqualTo(list)
        }
    }
}
