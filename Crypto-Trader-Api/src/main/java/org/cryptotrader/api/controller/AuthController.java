package org.cryptotrader.api.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.communication.request.LoginRequest;
import org.cryptotrader.api.library.communication.request.SignupRequest;
import org.cryptotrader.api.library.communication.response.AuthResponse;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.events.UserRegisteredEvent;
import org.cryptotrader.api.library.events.publisher.UserEventsPublisher;
import org.cryptotrader.api.library.extension.HttpRequestExtensionsKt;
import org.cryptotrader.api.library.model.dpop.DpopProofContext;
import org.cryptotrader.universal.library.model.http.AuthStatus;
import org.cryptotrader.universal.library.model.http.PayloadStatusResponse;
import org.cryptotrader.api.config.SecurityProperties;
import static org.cryptotrader.api.library.scripts.http.CookieScriptKt.buildRefreshCookie;
import static org.cryptotrader.api.library.scripts.http.CookieScriptKt.deleteCookie;
import org.cryptotrader.api.library.services.*;
import org.cryptotrader.api.library.component.dpop.DpopReplayCache;
import org.cryptotrader.api.library.services.dpop.DpopVerifierService;
import org.cryptotrader.api.library.services.jwt.JwtTokenService;
import org.cryptotrader.api.library.services.jwt.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Authentication API endpoints implementing DPoP-bound access tokens and
 * rotating refresh tokens.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
@PermitAll
public class AuthController {
    private final AuthService authService;
    private final UserEventsPublisher userEventsPublisher;
    private final ProductUserService productUserService;
    private final AuthContextService authContextService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final DpopReplayCache replayCache;
    private final DpopVerifierService dpopVerifier;
    private final SecurityProperties securityProperties;

    @Autowired
    public AuthController(AuthService authService,
                          UserEventsPublisher userEventsPublisher,
                          ProductUserService productUserService,
                          AuthContextService authContextService,
                          JwtTokenService jwtTokenService,
                          RefreshTokenService refreshTokenService,
                          DpopReplayCache replayCache,
                          DpopVerifierService dpopVerifier,
                          SecurityProperties securityProperties) {
        this.authService = authService;
        this.userEventsPublisher = userEventsPublisher;
        this.productUserService = productUserService;
        this.authContextService = authContextService;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.replayCache = replayCache;
        this.dpopVerifier = dpopVerifier;
        this.securityProperties = securityProperties;
    }

    /**
     * Registers a new user and initializes an authenticated session.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest signupRequest,
                                               @RequestHeader(value = "DPoP", required = false) String dpopProof,
                                               HttpServletRequest request) {
        if (this.authContextService.isAuthenticated()) {
            AuthResponse authResponse = new AuthResponse(AuthStatus.UNAUTHORIZED.isAuthorized);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
        }
        String jwkThumbprint = null;
        if (dpopProof != null) {
            jwkThumbprint = this.deriveJwkThumbprintFromProof(dpopProof, request, "POST");
            if (jwkThumbprint == null) {
                AuthResponse authResponse = new AuthResponse(false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
            }
        }
        PayloadStatusResponse<AuthResponse> signupResponse = this.authService.signup(signupRequest, jwkThumbprint);
        if (signupResponse.getPayload().isAuthorized()) {
            ProductUser possibleUser = this.productUserService.getUserByEmail(signupRequest.getEmail());
            // Issue refresh token cookie bound to jkt
            RefreshTokenService.RefreshTokenIssue issue = this.refreshTokenService.issue(possibleUser.getId(), jwkThumbprint);
            ResponseCookie cookie = buildRefreshCookie(this.refreshTokenService.cookieName(),
                                                                             issue.getId(),
                                                                             issue.getExpiresAt(),
                                                                             this.securityProperties.cookieSecure(),
                                                                             this.securityProperties.cookieSamesite());
            // Publish event
            UserRegisteredEvent registerEvent = new UserRegisteredEvent(possibleUser, LocalDateTime.now());
            this.userEventsPublisher.publishUserRegisteredEvent(registerEvent);
            return ResponseEntity.status(signupResponse.getStatus())
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(signupResponse.getPayload());
        }
        return ResponseEntity.status(signupResponse.getStatus()).body(signupResponse.getPayload());
    }

    /**
     * Authenticates user credentials and starts a session.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest,
                                              @RequestHeader(value = "DPoP", required = false) String dpopProof,
                                              HttpServletRequest request) {
        if (this.authContextService.isAuthenticated()) {
            AuthResponse authResponse = new AuthResponse(AuthStatus.UNAUTHORIZED.isAuthorized);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
        }
        boolean hasProof = this.dpopVerifier.isValidProof(dpopProof);
        AuthResponse unauthorizedResponse = new AuthResponse(false);
        if (!hasProof) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorizedResponse);
        }
        String jwkThumbprint = this.deriveJwkThumbprintFromProof(dpopProof, request, "POST");
        if (jwkThumbprint == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorizedResponse);
        }
        PayloadStatusResponse<AuthResponse> loginResponse = this.authService.login(loginRequest, jwkThumbprint);
        if (loginResponse.getPayload().isAuthorized()) {
            ProductUser user = this.productUserService.getUserByEmail(loginRequest.getEmail());
            RefreshTokenService.RefreshTokenIssue issue = this.refreshTokenService.issue(user.getId(), jwkThumbprint);
            ResponseCookie cookie = buildRefreshCookie(this.refreshTokenService.cookieName(),
                                                                             issue.getId(),
                                                                             issue.getExpiresAt(),
                                                                             this.securityProperties.cookieSecure(),
                                                                             this.securityProperties.cookieSamesite());
            return ResponseEntity.status(loginResponse.getStatus())
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(loginResponse.getPayload());
        }
        return ResponseEntity.status(loginResponse.getStatus()).body(loginResponse.getPayload());
    }

    /**
     * Rotates access and refresh tokens.
     *
     * Implementation Details:
     * - Validates the DPoP proof and the provided refresh cookie.
     * - Performs refresh token rotation (RTR) to prevent reuse of old tokens.
     * - Issues new tokens bound to the same public key thumbprint.
     * - Revokes the refresh token family on detection of suspicious activity.
     *
     * Headers:
     * - DPoP (required): A signed proof for this request.
     * Cookie:
     * - __Host-rt: Secure HttpOnly refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader(value = "DPoP", required = false) String dpopProof,
                                                HttpServletRequest request) {
        // Must have refresh cookie
        String cookieValue = HttpRequestExtensionsKt.readCookie(request, this.refreshTokenService.cookieName());
        if (cookieValue == null || cookieValue.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(false));
        }
        boolean hasProof = this.dpopVerifier.isValidProof(dpopProof);
        if (!hasProof) {
            ResponseCookie del = deleteCookie(this.refreshTokenService.cookieName(),
                                                                    this.securityProperties.cookieSecure(),
                                                                    this.securityProperties.cookieSamesite());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, del.toString())
                    .body(new AuthResponse(false));
        }
        String jwkThumbprint = this.deriveJwkThumbprintFromProof(dpopProof, request, "POST");
        if (jwkThumbprint == null) {
            // DPoP mandatory on refresh
            ResponseCookie cookieToDelete = deleteCookie(this.refreshTokenService.cookieName(),
                                                                               this.securityProperties.cookieSecure(),
                                                                               this.securityProperties.cookieSamesite());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, cookieToDelete.toString())
                    .body(new AuthResponse(false));
        }
        RefreshTokenService.RotationResult rotationResult = this.refreshTokenService.validateAndRotate(cookieValue, jwkThumbprint);
        if (rotationResult.getNewRecord() == null) {
            // reuse or invalid
            ResponseCookie cookieToDelete = deleteCookie(this.refreshTokenService.cookieName(),
                                                                               this.securityProperties.cookieSecure(),
                                                                               this.securityProperties.cookieSamesite());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, cookieToDelete.toString())
                    .body(new AuthResponse(false));
        }
        // Load user and issue new access token bound to same jkt
        long userId = rotationResult.getNewRecord().getUserId();
        ProductUser user = this.productUserService.getUserById(userId);
        String email = (user != null) ? user.getEmail() : null;
        if (email == null) {
            log.error("User not found for refresh token: {}", userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse(false));
        }
        String token = this.jwtTokenService.generateToken(String.valueOf(user.getId()), email, jwkThumbprint);
        AuthResponse payload = new AuthResponse(true, token);
        ResponseCookie cookie = buildRefreshCookie(this.refreshTokenService.cookieName(),
                                                                         rotationResult.getNewRecord().getId(),
                                                                         rotationResult.getNewRecord().getExpiresAt(),
                                                                         this.securityProperties.cookieSecure(),
                                                                         this.securityProperties.cookieSamesite());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(payload);
    }

    /**
     * Quick status check used by the UI.
     * Returns authorized=true if the current request is authenticated.
     */
    @GetMapping("/logged-in")
    public ResponseEntity<AuthResponse> isLoggedIn() {
        boolean authenticated = this.authContextService.isAuthenticated();
        AuthResponse authResponse = new AuthResponse(authenticated);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Terminates the current session and revokes tokens.
     *
     * @param dpopProof Optional DPoP proof to validate the request origin.
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestHeader(value = "DPoP", required = false) String dpopProof,
                                               HttpServletRequest request) {
        // DPoP is optional for logout: if provided and valid, great; if missing/invalid, we still end the session.
        // Attempt to derive jkt only to validate the proof when present; ignore failures.
        boolean isValidProof = this.dpopVerifier.isValidProof(dpopProof);
        if (isValidProof) {
            try {
                this.deriveJwkThumbprintFromProof(dpopProof, request, "POST");
            } catch (Exception _) {
                // Ignore proof validation failures
            }
        }
        // Clear refresh cookie and revoke its family
        String cookieValue = HttpRequestExtensionsKt.readCookie(request, this.refreshTokenService.cookieName());
        ResponseCookie cookieToDelete = deleteCookie(this.refreshTokenService.cookieName(),
                                                                           this.securityProperties.cookieSecure(),
                                                                           this.securityProperties.cookieSamesite());
        if (cookieValue != null && !cookieValue.isBlank()) {
            this.refreshTokenService.revokeByTokenId(cookieValue);
        }
        // Blacklist presented access token if any
        this.authContextService.logout();
        AuthResponse authResponse = new AuthResponse(AuthStatus.UNAUTHORIZED.isAuthorized);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookieToDelete.toString()).body(authResponse);
    }

    @GetMapping("/logout")
    public ResponseEntity<AuthResponse> logoutGet(@RequestHeader(value = "DPoP", required = false) String dpopProof,
                                                  HttpServletRequest request) {
        return logout(dpopProof, request);
    }



    /**
     * Derives the JWK thumbprint (jkt) from a DPoP proof according to RFC 9449.
     *
     * This method validates the signature and claims of the DPoP proof and returns
     * the cryptographic thumbprint of the public key.
     *
     * @param dpopProof      The signed DPoP proof JWT.
     * @param request        The current HttpServletRequest for context validation.
     * @param expectedMethod The expected HTTP method of the request.
     * @return The jkt (thumbprint) if verification succeeds; otherwise null.
     */
    private String deriveJwkThumbprintFromProof(String dpopProof, HttpServletRequest request, String expectedMethod) {
        try {
            // If the pre-JWT DPoPValidationFilter has already verified the proof, reuse its context
            Object context = (request != null) ? request.getAttribute("dpop.proof") : null;
            if (context instanceof DpopProofContext proofContext) {
                return proofContext.getKeyThumbprint();
            }
            // Otherwise, verify here (including replay check)
            DpopVerifierService.VerificationResult verification = this.dpopVerifier.verify(dpopProof,
                                                                                           expectedMethod,
                                                                                           HttpRequestExtensionsKt.fullUrl(request),
                                                                                           null,
                                                                                           20L);
            if (verification == null) {
                return null;
            }
            if (this.replayCache.isReplay(verification.getJwtId())) {
                return null;
            }
            return verification.getJwkThumbprint();
        } catch (Exception ex) {
            log.debug("Failed to derive jkt from DPoP proof", ex);
        }
        return null;
    }

    // Convenience overloads for unit tests and backward compatibility (not HTTP endpoints)
    /**
     * Overload without DPoP or request argument for tests.
     */
    @Deprecated(forRemoval = true)
    public ResponseEntity<AuthResponse> signup(SignupRequest signupRequest) {
        return signup(signupRequest, null, null);
    }

    /**
     * Overload without DPoP or request argument for tests.
     */
    @Deprecated(forRemoval = true)
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        return login(loginRequest, null, null);
    }

    /**
     * Overload without DPoP or request argument for tests.
     */
    @Deprecated(forRemoval = true)
    public ResponseEntity<AuthResponse> logout() {
        return logout(null, null);
    }
}
