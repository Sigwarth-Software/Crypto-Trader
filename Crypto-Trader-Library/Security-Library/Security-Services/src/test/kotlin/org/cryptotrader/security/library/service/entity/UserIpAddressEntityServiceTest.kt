package org.cryptotrader.security.library.service.entity

import java.time.LocalDateTime
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.security.library.entity.ip.UserIpAddress
import org.cryptotrader.security.library.repository.UserIpAddressRepository
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

@DisplayName("UserIpAddressEntityService")
@UnitSuite
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
    @DisplayName("recordUserIp")
    inner class RecordUserIp {

        @Nested
        @DisplayName("when IP does not exist for user")
        inner class WhenIPDoesNotExistForUser {
            @BeforeEach
            fun setUp() {
                `when`(repository.findByUserAndIpAddress(testUser, testIp)).thenReturn(Optional.empty())
                val savedIp = UserIpAddress(testUser, testIp)
                `when`(repository.save(any(UserIpAddress::class.java))).thenReturn(savedIp)
            }

            @Test
            @DisplayName("should create new UserIpAddress record")
            fun createNewUserIpAddressRecord() {
                val result = service.recordUserIp(testUser, testIp)

                val captor = ArgumentCaptor.forClass(UserIpAddress::class.java)
                verify(repository, times(1)).save(captor.capture())
                assertThat(captor.value.user).isEqualTo(testUser)
                assertThat(captor.value.ipAddress).isEqualTo(testIp)
                assertThat(captor.value.lastAccessed).isNotNull
                assertThat(result).isNotNull
            }
        }

        @Nested
        @DisplayName("when IP already exists for user")
        inner class WhenIPAlreadyExistsForUser {
            private lateinit var previousTime: LocalDateTime
            private lateinit var existing: UserIpAddress

            @BeforeEach
            fun setUp() {
                previousTime = LocalDateTime.now().minusDays(1)
                existing = UserIpAddress(testUser, testIp, previousTime)
                `when`(repository.findByUserAndIpAddress(testUser, testIp)).thenReturn(Optional.of(existing))
                `when`(repository.save(any(UserIpAddress::class.java))).thenAnswer { it.getArgument(0) }
            }

            @Test
            @DisplayName("should update lastAccessed")
            fun updateLastAccessed() {
                val result = service.recordUserIp(testUser, testIp)

                verify(repository, times(1)).save(existing)
                assertThat(result.lastAccessed).isAfter(previousTime)
            }
        }
    }

    @Nested
    @DisplayName("findByUserAndIpAddress")
    inner class FindByUserAndIpAddress {
        @Test
        @DisplayName("should find by user and IP address")
        fun findByUserAndIPAddress() {
            val expected = UserIpAddress(testUser, testIp)
            `when`(repository.findByUserAndIpAddress(testUser, testIp)).thenReturn(Optional.of(expected))

            val result = service.findByUserAndIpAddress(testUser, testIp)

            assertThat(result).isPresent
            assertThat(result.get()).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("findByUserIdAndIpAddress")
    inner class FindByUserIdAndIpAddress {
        @Test
        @DisplayName("should find by userId and IP address")
        fun findByUserIdAndIPAddress() {
            val expected = UserIpAddress(testUser, testIp)
            `when`(repository.findByUserIdAndIpAddress(1L, testIp)).thenReturn(Optional.of(expected))

            val result = service.findByUserIdAndIpAddress(1L, testIp)

            assertThat(result).isPresent
            assertThat(result.get()).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("findAllByUser")
    inner class FindAllByUser {
        @Test
        @DisplayName("should find all by user")
        fun findAllByUser() {
            val list = listOf(UserIpAddress(testUser, testIp))
            `when`(repository.findAllByUser(testUser)).thenReturn(list)

            val result = service.findAllByUser(testUser)

            assertThat(result).isEqualTo(list)
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    inner class FindAllByUserId {
        @Test
        @DisplayName("should find all by userId")
        fun findAllByUserId() {
            val list = listOf(UserIpAddress(testUser, testIp))
            `when`(repository.findAllByUserId(1L)).thenReturn(list)

            val result = service.findAllByUserId(1L)

            assertThat(result).isEqualTo(list)
        }
    }

    @Nested
    @DisplayName("findAllByIpAddress")
    inner class FindAllByIpAddress {
        @Test
        @DisplayName("should find all by IP address")
        fun findAllByIPAddress() {
            val list = listOf(UserIpAddress(testUser, testIp))
            `when`(repository.findAllByIpAddress(testIp)).thenReturn(list)

            val result = service.findAllByIpAddress(testIp)

            assertThat(result).isEqualTo(list)
        }
    }
}
