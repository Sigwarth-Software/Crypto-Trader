package org.cryptotrader.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.cryptotrader.api.library.communication.request.LoginRequest;
import org.cryptotrader.api.library.communication.request.SignupRequest;
import org.cryptotrader.api.library.communication.response.AuthResponse;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.entity.user.SafePassword;
import org.cryptotrader.api.library.events.publisher.UserEventsPublisher;
import org.cryptotrader.api.library.services.AuthContextService;
import org.cryptotrader.api.library.services.AuthService;
import org.cryptotrader.api.library.services.ProductUserService;
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite;
import org.cryptotrader.universal.library.model.http.PayloadStatusResponse;
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
import static org.mockito.Mockito.*;

@DisplayName("AuthController")
@UnitSuite
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
    private String mockDpopProof;
    private HttpServletRequest mockHttpServletRequest;

    @BeforeEach
    void setUp() {
        this.signupRequest = new SignupRequest("ollie@ollie.com", "password");
        this.loginRequest = new LoginRequest("ollie@ollie.com", "password");
        // Something that looks like a DPoP proof, but is just random chars
        // for testing.
        this.mockDpopProof = UUID.randomUUID().toString();
        this.mockHttpServletRequest = mock(HttpServletRequest.class);
    }

    @Nested
    @DisplayName("signup")
    class Signup {
        @Nested
        @DisplayName("when a user is in session")
        class WhenAUserIsInSession {
            @BeforeEach
            void setUp() throws Exception {
                when(authContextService.isAuthenticated()).thenReturn(true);
            }

            @Test
            @DisplayName("should not sign up users in session")
            public void notSignUpUsersInSession() {
                final ResponseEntity<AuthResponse> signupResponse = authController.signup(signupRequest, mockDpopProof, mockHttpServletRequest);
                verify(authContextService).isAuthenticated();
                final HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
                final AuthResponse expectedResponse = new AuthResponse(false);
                assertEquals(expectedStatus, signupResponse.getStatusCode());
                assertEquals(expectedResponse, signupResponse.getBody());
            }
        }

        @DisplayName("when a user is not in session")
        @Nested
        class WhenUserNotInSession {
            @BeforeEach
            void setUp() throws Exception {
                when(authContextService.isAuthenticated()).thenReturn(false);
                when(authService.signup(signupRequest)).thenReturn(new PayloadStatusResponse<>(new AuthResponse(true), HttpStatus.OK));
                when(productUserService.getUserByEmail("ollie@ollie.com")).thenReturn(new ProductUser("Ollie", new SafePassword("password")));
            }

            @Test
            @DisplayName("should sign up users not in session")
            @Disabled
            public void signUpUsersNotInSession() {
                final ResponseEntity<AuthResponse> signupResponse = authController.signup(signupRequest, mockDpopProof, mockHttpServletRequest);
                verify(authContextService).isAuthenticated();
                verify(authService).signup(signupRequest);
                final HttpStatus expectedStatus = HttpStatus.OK;
                final AuthResponse expectedResponse = new AuthResponse(true);
                assertEquals(expectedStatus, signupResponse.getStatusCode());
                assertEquals(expectedResponse, signupResponse.getBody());
            }
        }
    }

    @Nested
    @DisplayName("login")
    class Login {
        @Nested
        @DisplayName("when a user is not in session")
        class WhenUserNotInSession {
            @BeforeEach
            void setUp() throws Exception {
                when(authContextService.isAuthenticated()).thenReturn(false);
                when(authService.login(loginRequest)).thenReturn(new PayloadStatusResponse<>(new AuthResponse(true), HttpStatus.OK));
            }

            @Test
            @DisplayName("should login users not in session")
            @Disabled
            public void loginUsersNotInSession() {
                final ResponseEntity<AuthResponse> loginResponse = authController.login(loginRequest, mockDpopProof, mockHttpServletRequest);
                verify(authContextService).isAuthenticated();
                verify(authService).login(loginRequest);
                final HttpStatus expectedStatus = HttpStatus.OK;
                final AuthResponse expectedResponse = new AuthResponse(true);
                assertEquals(expectedStatus, loginResponse.getStatusCode());
                assertEquals(expectedResponse, loginResponse.getBody());
            }
        }

        @Nested
        @DisplayName("when a user is in session")
        class WhenUserInSession {
            @BeforeEach
            void setUp() throws Exception {
                when(authContextService.isAuthenticated()).thenReturn(true);
            }

            @Test
            @DisplayName("should not login users in session")
            public void notLoginUsersInSession() {
                final ResponseEntity<AuthResponse> loginResponse =
                    authController.login(
                        loginRequest,
                        mockDpopProof,
                        mockHttpServletRequest
                    );
                verify(authContextService).isAuthenticated();
                final HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
                final AuthResponse expectedResponse = new AuthResponse(false);
                assertEquals(expectedStatus, loginResponse.getStatusCode());
                assertEquals(expectedResponse, loginResponse.getBody());
            }
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {
        @Nested
        @DisplayName("when a user is in session")
        class WhenUserInSession {
            @BeforeEach
            void setUp() {
                when(authContextService.isAuthenticated()).thenReturn(true);
            }

            @Test
            @DisplayName("should logout users in session")
            @Disabled
            public void logoutUsersInSession() {
                final ResponseEntity<AuthResponse> logoutResponse =
                    authController.logout(
                        mockDpopProof,
                        mockHttpServletRequest
                    );
                verify(authContextService).isAuthenticated();
                final HttpStatus expectedStatus = HttpStatus.OK;
                final AuthResponse expectedResponse = new AuthResponse(false);
                assertEquals(expectedStatus, logoutResponse.getStatusCode());
                assertEquals(expectedResponse, logoutResponse.getBody());
            }
        }

        @Nested
        @DisplayName("when a user is not in session")
        class WhenUserNotInSession {
            @BeforeEach
            void setUp() throws Exception {
                when(authContextService.isAuthenticated()).thenReturn(false);
            }

            @Test
            @DisplayName("should not logout users")
            @Disabled
            public void notLogoutUsers() {
                final ResponseEntity<AuthResponse> logoutResponse = authController.logout(mockDpopProof, mockHttpServletRequest);
                verify(authContextService).isAuthenticated();
                final HttpStatus expectedStatus = HttpStatus.OK;
                final AuthResponse expectedResponse = new AuthResponse(false);
                assertEquals(expectedStatus, logoutResponse.getStatusCode());
                assertEquals(expectedResponse, logoutResponse.getBody());
            }
        }
    }

    @Nested
    @DisplayName("isLoggedIn")
    class IsLoggedIn {
        @Nested
        @DisplayName("when a user is in session")
        class WhenAUserIsInSession {
            @BeforeEach
            void setUp() throws Exception {
                when(authContextService.isAuthenticated()).thenReturn(true);
            }

            @Test
            @DisplayName("should count the user as logged in")
            public void countTheUserAsLoggedIn() {
                final ResponseEntity<AuthResponse> isLoggedInResponse = authController.isLoggedIn();
                verify(authContextService).isAuthenticated();
                final HttpStatus expectedStatus = HttpStatus.OK;
                final AuthResponse expectedResponse = new AuthResponse(true);
                assertEquals(expectedStatus, isLoggedInResponse.getStatusCode());
                assertEquals(expectedResponse, isLoggedInResponse.getBody());
            }
        }

        @Nested
        @DisplayName("when a user is not in session")
        class WhenAUserIsNotInSession {
            @BeforeEach
            void setUp() throws Exception {
                when(authContextService.isAuthenticated()).thenReturn(false);
            }

            @Test
            @DisplayName("should not count the user as logged in")
            public void notCountTheUserAsLoggedIn() {
                final ResponseEntity<AuthResponse> isLoggedInResponse = authController.isLoggedIn();
                verify(authContextService).isAuthenticated();
                final HttpStatus expectedStatus = HttpStatus.OK;
                final AuthResponse expectedResponse = new AuthResponse(false);
                assertEquals(expectedStatus, isLoggedInResponse.getStatusCode());
                assertEquals(expectedResponse, isLoggedInResponse.getBody());
            }
        }
    }
}
