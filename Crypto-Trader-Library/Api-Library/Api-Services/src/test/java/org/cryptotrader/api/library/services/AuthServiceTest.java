package org.cryptotrader.api.library.services;

import org.cryptotrader.api.library.communication.request.LoginRequest;
import org.cryptotrader.api.library.communication.request.SignupRequest;
import org.cryptotrader.api.library.communication.request.UserRequest;
import org.cryptotrader.api.library.communication.response.AuthResponse;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.entity.user.SafePassword;
import org.cryptotrader.api.library.events.publisher.UserEventsPublisher;
import org.cryptotrader.api.library.services.jwt.JwtTokenService;
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite;
import org.cryptotrader.universal.library.model.http.PayloadStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AuthService")
@UnitSuite
public class AuthServiceTest extends CryptoTraderTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private ProductUserService productUserService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private UserEventsPublisher userEventsPublisher;

    @Mock
    private JwtTokenService jwtService;

    private LoginRequest loginRequest;

    private SignupRequest signupRequest;

    private UserRequest userRequest;

    private ProductUser user;

    private String testUsername;
    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        this.testUsername = "Ollie";
        this.testEmail = "ollie@ollie.com";
        this.testPassword = "password";
        final SafePassword clientSideHashedPassword = new SafePassword(this.testPassword);
        final String encodedPassword = clientSideHashedPassword.getEncodedPassword();
        this.userRequest = new UserRequest(this.testUsername, this.testEmail, encodedPassword);
        this.loginRequest = new LoginRequest(this.testEmail, encodedPassword);
        this.signupRequest = new SignupRequest(this.testEmail, encodedPassword);
        this.user = new ProductUser(this.testUsername, new SafePassword(this.testPassword));
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {
        @Nested
        @DisplayName("when valid dependencies are provided")
        class WhenValidDependenciesAreProvided {
            @Test
            @DisplayName("should instantiate object")
            public void instantiateObject() {
                final ProductUserService productUserService = Mockito.mock(ProductUserService.class);
                final PortfolioService portfolioService = Mockito.mock(PortfolioService.class);
                final UserEventsPublisher userEventsPublisher = Mockito.mock(UserEventsPublisher.class);
                final JwtTokenService jwtService = Mockito.mock(JwtTokenService.class);
                final AuthService authService = new AuthService(productUserService, portfolioService, userEventsPublisher, jwtService);
                assertNotNull(authService);
            }
        }
    }

    @Nested
    @DisplayName("signup")
    class Signup {
        @Nested
        @DisplayName("when the user does not exist")
        class WhenTheUserDoesNotExist {
            @BeforeEach
            void setUp() throws Exception {
                when(productUserService.userExistsByEmail(testEmail)).thenReturn(false);
                when(jwtService.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.isNull())).thenReturn("signed.jwt.token");
            }

            @Test
            @DisplayName("should sign up users with valid requests")
            public void signUpUsersWithValidRequests() {
                final PayloadStatusResponse<AuthResponse> actualResponse = authService.signup(signupRequest);
                verify(productUserService).userExistsByEmail(testEmail);
                final AuthResponse actualAuthResponse = actualResponse.getPayload();
                assertTrue(actualAuthResponse.isAuthorized());
                assertNotNull(actualAuthResponse.getToken());
            }
        }

        @Nested
        @DisplayName("when user exists")
        class WhenUserExists {
            @BeforeEach
            void setUp() throws Exception {
                when(productUserService.userExistsByEmail(testEmail)).thenReturn(true);
            }

            @Test
            @DisplayName("should not sign up existing users")
            public void notSignUpExistingUsers() {
                AuthResponse expectedResponse = new AuthResponse(false);
                final PayloadStatusResponse<AuthResponse> actualResponse = authService.signup(signupRequest);
                final AuthResponse actualAuthResponse = actualResponse.getPayload();
                assertEquals(expectedResponse.isAuthorized(), actualAuthResponse.isAuthorized());
            }
        }
    }

    @Nested
    @DisplayName("login")
    class Login {
        @Nested
        @DisplayName("when the user exists and the password matches")
        class WhenTheUserExistsAndThePasswordMatches {
            @BeforeEach
            void setUp() throws Exception {
                when(productUserService.getUserByEmail(testEmail)).thenReturn(user);
                when(productUserService.comparePassword(user, userRequest.getPassword())).thenReturn(true);
                when(jwtService.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.isNull())).thenReturn("signed.jwt.token");
            }

            @Test
            @DisplayName("should login users with valid requests")
            public void loginUsersWithValidRequests() {
                final PayloadStatusResponse<AuthResponse> actualResponse = authService.login(loginRequest);
                verify(productUserService).getUserByEmail(testEmail);
                verify(productUserService).comparePassword(user, userRequest.getPassword());
                final AuthResponse payload = actualResponse.getPayload();
                assertTrue(payload.isAuthorized());
                assertNotNull(payload.getToken());
            }
        }

        @Nested
        @DisplayName("when the user does not exist")
        class WhenTheUserDoesNotExist {
            @BeforeEach
            void setUp() throws Exception {
                when(productUserService.getUserByEmail(testEmail)).thenReturn(null);
            }

            @Test
            @DisplayName("should not login users with invalid requests")
            public void notLoginUsersWithInvalidRequests() {
                AuthResponse expectedResponse = new AuthResponse(false);
                final PayloadStatusResponse<AuthResponse> actualResponse = authService.login(loginRequest);
                verify(productUserService).getUserByEmail(testEmail);
                assertEquals(expectedResponse, actualResponse.getPayload());
            }
        }

        @Nested
        @DisplayName("when the password does not match")
        class WhenThePasswordDoesNotMatch {
            @BeforeEach
            void setUp() throws Exception {
                when(productUserService.getUserByEmail(testEmail)).thenReturn(user);
                when(productUserService.comparePassword(user, userRequest.getPassword())).thenReturn(false);
            }

            @Test
            @DisplayName("should not login users with mismatched passwords")
            public void notLoginUsersWithMismatchedPasswords() {
                AuthResponse expectedResponse = new AuthResponse(false);
                final PayloadStatusResponse<AuthResponse> actualResponse = authService.login(loginRequest);
                verify(productUserService).getUserByEmail(testEmail);
                verify(productUserService).comparePassword(user, userRequest.getPassword());
                assertEquals(expectedResponse, actualResponse.getPayload());
            }
        }
    }
}
