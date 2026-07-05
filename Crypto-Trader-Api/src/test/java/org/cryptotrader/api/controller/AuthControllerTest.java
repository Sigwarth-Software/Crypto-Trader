package org.cryptotrader.api.controller;

import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.services.AuthContextService;
import org.cryptotrader.api.library.services.AuthService;
import org.cryptotrader.api.library.services.ProductUserService;
import org.cryptotrader.api.library.events.publisher.UserEventsPublisher;
import org.cryptotrader.api.library.communication.request.SignupRequest;
import org.cryptotrader.api.library.communication.request.LoginRequest;
import org.cryptotrader.api.library.communication.response.AuthResponse;
import org.cryptotrader.universal.library.model.http.PayloadStatusResponse;
import org.cryptotrader.api.library.entity.user.SafePassword;
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Auth Controller")
public class AuthControllerTest extends CryptoTraderTest {
    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @Mock
    private ProductUserService productUserService;

    @Mock
    private UserEventsPublisher userEventsPublisher;

    @Mock
    private AuthContextService authContextService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setup() {
        this.signupRequest = new SignupRequest("ollie@ollie.com", "password");
        this.loginRequest = new LoginRequest("ollie@ollie.com", "password");
    }

    @Nested
    @DisplayName("Signup")
    class Signup {
        @Test
        @DisplayName("Should not sign up users in session")
        public void signup_NotSignUp_UsersInSession() {
            when(authContextService.isAuthenticated()).thenReturn(true);
            ResponseEntity<AuthResponse> signupResponse = authController.signup(signupRequest);
            verify(authContextService).isAuthenticated();
            HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
            AuthResponse expectedResponse = new AuthResponse(false);
            assertEquals(expectedStatus, signupResponse.getStatusCode());
            assertEquals(expectedResponse, signupResponse.getBody());
        }

        @Test
        @DisplayName("Should sign up users not in session")
        @Disabled
        public void signup_SignUp_UsersNotInSession() {
            when(authContextService.isAuthenticated()).thenReturn(false);
            when(authService.signup(signupRequest)).thenReturn(new PayloadStatusResponse<>(new AuthResponse(true), HttpStatus.OK));
            when(productUserService.getUserByEmail("ollie@ollie.com")).thenReturn(new ProductUser("Ollie", new SafePassword("password")));
            ResponseEntity<AuthResponse> signupResponse = authController.signup(signupRequest);
            verify(authContextService).isAuthenticated();
            verify(authService).signup(signupRequest);
            HttpStatus expectedStatus = HttpStatus.OK;
            AuthResponse expectedResponse = new AuthResponse(true);
            assertEquals(expectedStatus, signupResponse.getStatusCode());
            assertEquals(expectedResponse, signupResponse.getBody());
        }
    }

    @Nested
    @DisplayName("Login")
    class Login {
        @Test
        @DisplayName("Should not login users in session")
        public void login_NotLogin_UsersInSession() {
            when(authContextService.isAuthenticated()).thenReturn(true);
            ResponseEntity<AuthResponse> loginResponse = authController.login(loginRequest);
            verify(authContextService).isAuthenticated();
            HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
            AuthResponse expectedResponse = new AuthResponse(false);
            assertEquals(expectedStatus, loginResponse.getStatusCode());
            assertEquals(expectedResponse, loginResponse.getBody());
        }

        @Test
        @DisplayName("Should login users not in session")
        @Disabled
        public void login_Login_UsersNotInSession() {
            when(authContextService.isAuthenticated()).thenReturn(false);
            when(authService.login(loginRequest)).thenReturn(new PayloadStatusResponse<>(new AuthResponse(true), HttpStatus.OK));
            ResponseEntity<AuthResponse> loginResponse = authController.login(loginRequest);
            verify(authContextService).isAuthenticated();
            verify(authService).login(loginRequest);
            HttpStatus expectedStatus = HttpStatus.OK;
            AuthResponse expectedResponse = new AuthResponse(true);
            assertEquals(expectedStatus, loginResponse.getStatusCode());
            assertEquals(expectedResponse, loginResponse.getBody());
        }
    }

    @Nested
    @DisplayName("Logout")
    class Logout {
        @Test
        @DisplayName("Should logout users in session")
        @Disabled
        public void logout_Logout_UsersInSession() {
            when(authContextService.isAuthenticated()).thenReturn(true);
            ResponseEntity<AuthResponse> logoutResponse = authController.logout();
            verify(authContextService).isAuthenticated();
            HttpStatus expectedStatus = HttpStatus.OK;
            AuthResponse expectedResponse = new AuthResponse(false);
            assertEquals(expectedStatus, logoutResponse.getStatusCode());
            assertEquals(expectedResponse, logoutResponse.getBody());
        }

        @Test
        @DisplayName("Should not logout users not in session")
        @Disabled
        public void logout_NotLogout_UsersNotInSession() {
            when(authContextService.isAuthenticated()).thenReturn(false);
            ResponseEntity<AuthResponse> logoutResponse = authController.logout();
            verify(authContextService).isAuthenticated();
            HttpStatus expectedStatus = HttpStatus.OK;
            AuthResponse expectedResponse = new AuthResponse(false);
            assertEquals(expectedStatus, logoutResponse.getStatusCode());
            assertEquals(expectedResponse, logoutResponse.getBody());
        }
    }

    @Nested
    @DisplayName("Is Logged In")
    class IsLoggedIn {
        @Test
        @DisplayName("Should count session users as logged in")
        public void isLoggedIn_LoggedIn_UsersInSession() {
            when(authContextService.isAuthenticated()).thenReturn(true);
            ResponseEntity<AuthResponse> isLoggedInResponse = authController.isLoggedIn();
            verify(authContextService).isAuthenticated();
            HttpStatus expectedStatus = HttpStatus.OK;
            AuthResponse expectedResponse = new AuthResponse(true);
            assertEquals(expectedStatus, isLoggedInResponse.getStatusCode());
            assertEquals(expectedResponse, isLoggedInResponse.getBody());
        }

        @Test
        @DisplayName("Should not count unauthorized session users as logged in")
        public void isLoggedIn_NotLoggedIn_UsersInSession() {
            when(authContextService.isAuthenticated()).thenReturn(false);
            ResponseEntity<AuthResponse> isLoggedInResponse = authController.isLoggedIn();
            verify(authContextService).isAuthenticated();
            HttpStatus expectedStatus = HttpStatus.OK;
            AuthResponse expectedResponse = new AuthResponse(false);
            assertEquals(expectedStatus, isLoggedInResponse.getStatusCode());
            assertEquals(expectedResponse, isLoggedInResponse.getBody());
        }
    }
}
