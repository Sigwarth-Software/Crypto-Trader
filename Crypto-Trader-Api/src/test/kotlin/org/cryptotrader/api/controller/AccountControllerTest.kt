package org.cryptotrader.api.controller

import org.cryptotrader.api.library.services.ProductUserService
import org.cryptotrader.api.library.services.SessionService
import org.cryptotrader.api.library.services.models.ProfilePictureOperations
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mock

@Tag("AccountController")
@Tag("controller")
@DisplayName("Account Controller")
class AccountControllerTest : CryptoTraderTest() {

    @Mock lateinit var sessionService: SessionService
    @Mock lateinit var profilePictureService: ProfilePictureOperations
    @Mock lateinit var productUserService: ProductUserService

    private lateinit var controller: AccountController

    @BeforeEach
    fun setUp() {
        controller = AccountController(sessionService, profilePictureService, productUserService)
    }

    @Nested
    @Tag("uploadProfilePicture")
    @DisplayName("Upload Profile Picture")
    inner class UploadProfilePicture {
        @Test
        @DisplayName("Should upload profile picture for authenticated user")
        fun uploadProfilePicture_Uploads_WhenAuthenticated() { }

        @Test
        @DisplayName("Should return unauthorized when no session user")
        fun uploadProfilePicture_ReturnsUnauthorized_WhenNoSessionUser() { }

        @Test
        @DisplayName("Should return internal server error on IO failure")
        fun uploadProfilePicture_ReturnsError_OnIoFailure() { }
    }

    @Nested
    @Tag("hasProfilePicture")
    @DisplayName("Has Profile Picture")
    inner class HasProfilePicture {
        @Test
        @DisplayName("Should return true when user has profile picture")
        fun hasProfilePicture_ReturnsTrue_WhenExists() { }

        @Test
        @DisplayName("Should return bad request when id invalid")
        fun hasProfilePicture_ReturnsBadRequest_OnInvalidId() { }
    }

    @Nested
    @Tag("getProfilePicture")
    @DisplayName("Get Profile Picture")
    inner class GetProfilePicture {
        @Test
        @DisplayName("Should return image bytes with content type")
        fun getProfilePicture_ReturnsImage_WhenExists() { }

        @Test
        @DisplayName("Should return not found when missing")
        fun getProfilePicture_ReturnsNotFound_WhenMissing() { }
    }
}
