package org.cryptotrader.api.library.services;

import org.cryptotrader.api.library.communication.request.LoginRequest;
import org.cryptotrader.api.library.communication.request.SignupRequest;
import org.cryptotrader.api.library.events.publisher.UserEventsPublisher;
import org.cryptotrader.api.library.communication.request.UserRequest;
import org.cryptotrader.api.library.communication.response.AuthResponse;
import org.cryptotrader.api.library.entity.user.SafePassword;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.universal.library.model.http.PayloadStatusResponse;
import org.cryptotrader.api.library.services.jwt.JwtTokenService;
import org.cryptotrader.testing.library.infrastructure.CryptoTraderTest;
import org.junit.jupiter.api.AfterAll;
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

@DisplayName("Auth Service")
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
    void setup() {
        this.testUsername = "Ollie";
        this.testEmail = "ollie@ollie.com";
        this.testPassword = "password";
        SafePassword clientSideHashedPassword = new SafePassword(this.testPassword);
        String encodedPassword = clientSideHashedPassword.getEncodedPassword();
        this.userRequest = new UserRequest(this.testUsername, this.testEmail, encodedPassword);
        this.loginRequest = new LoginRequest(this.testEmail, encodedPassword);
        this.signupRequest = new SignupRequest(this.testEmail, encodedPassword);
        this.user = new ProductUser(this.testUsername, new SafePassword(this.testPassword));
    }

    @AfterAll
    static void tearDown() {

    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {
        @Test
        @DisplayName("Constructor should instantiate object")
        public void constructor_InstantiatesAuthService_WhenValidArgumentsProvided() {
            ProductUserService productUserService = Mockito.mock(ProductUserService.class);
            PortfolioService portfolioService = Mockito.mock(PortfolioService.class);
            UserEventsPublisher userEventsPublisher = Mockito.mock(UserEventsPublisher.class);
            JwtTokenService jwtService = Mockito.mock(JwtTokenService.class);
            AuthService authService = new AuthService(productUserService, portfolioService, userEventsPublisher, jwtService);
            assertNotNull(authService);
        }
    }

    @Nested
    @DisplayName("Signup")
    class Signup {
        @Test
        @DisplayName("Should sign up users with valid requests")
        public void signup_SignUpUsers_WithValidRequests() {
            when(productUserService.userExistsByEmail(testEmail)).thenReturn(false);
            when(jwtService.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.isNull())).thenReturn("signed.jwt.token");
            PayloadStatusResponse<AuthResponse> actualResponse = authService.signup(signupRequest);
            verify(productUserService).userExistsByEmail(testEmail);
            AuthResponse actualAuthResponse = actualResponse.getPayload();
            assertTrue(actualAuthResponse.isAuthorized());
            assertNotNull(actualAuthResponse.getToken());
        }

        @Test
        @DisplayName("Should not sign up existing users")
        public void signup_NotSignUp_WhenUserExists() {
            when(productUserService.userExistsByEmail(testEmail)).thenReturn(true);
            AuthResponse expectedResponse = new AuthResponse(false);
            PayloadStatusResponse<AuthResponse> actualResponse = authService.signup(signupRequest);
            AuthResponse actualAuthResponse = actualResponse.getPayload();
            assertEquals(expectedResponse.isAuthorized(), actualAuthResponse.isAuthorized());
        }
    }

    @Nested
    @DisplayName("Login")
    class Login {
        @Test
        @DisplayName("Should login users with valid requests")
        public void login_LoginUsers_WithValidRequests() {
            when(productUserService.getUserByEmail(testEmail)).thenReturn(user);
            when(productUserService.comparePassword(user, userRequest.getPassword())).thenReturn(true);
            when(jwtService.generateToken(Mockito.anyString(), Mockito.anyString(), Mockito.isNull())).thenReturn("signed.jwt.token");
            PayloadStatusResponse<AuthResponse> actualResponse = authService.login(loginRequest);
            verify(productUserService).getUserByEmail(testEmail);
            verify(productUserService).comparePassword(user, userRequest.getPassword());
            AuthResponse payload = actualResponse.getPayload();
            assertTrue(payload.isAuthorized());
            assertNotNull(payload.getToken());
        }

        @Test
        @DisplayName("Should not login users with invalid requests")
        public void login_NotLoginUsers_WithInvalidRequests() {
            when(productUserService.getUserByEmail(testEmail)).thenReturn(null);
            AuthResponse expectedResponse = new AuthResponse(false);
            PayloadStatusResponse<AuthResponse> actualResponse = authService.login(loginRequest);
            verify(productUserService).getUserByEmail(testEmail);
            assertEquals(expectedResponse, actualResponse.getPayload());
        }

        @Test
        @DisplayName("Should not login users with mismatched passwords")
        public void login_NotLoginUsers_WithMismatchedPasswords() {
            when(productUserService.getUserByEmail(testEmail)).thenReturn(user);
            AuthResponse expectedResponse = new AuthResponse(false);
            when(productUserService.comparePassword(user, userRequest.getPassword())).thenReturn(false);
            PayloadStatusResponse<AuthResponse> actualResponse = authService.login(loginRequest);
            verify(productUserService).getUserByEmail(testEmail);
            verify(productUserService).comparePassword(user, userRequest.getPassword());
            assertEquals(expectedResponse, actualResponse.getPayload());
        }
    }
}
