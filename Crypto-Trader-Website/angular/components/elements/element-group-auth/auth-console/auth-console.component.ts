import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core'
import { Router } from '@angular/router'

import { AuthPopup, AuthType } from '@theoliverlear/angular-suite'
import { LoginService } from '@http/auth/access/login.service'
import { SignupService } from '@http/auth/access/signup.service'
import { TokenStorageService } from '@auth/token-storage.service'
import { LoginCredentials } from '@models/auth/LoginCredentials'
import { SignupCredentials } from '@models/auth/SignupCredentials'
import { AuthResponse, LoginRequest, SignupRequest } from '@models/auth/types'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { LoggerContext } from '@models/logging/LoggerContext'

/**
 * Authentication console component for the landing/authorize page.
 */
@Component({
    selector: 'auth-console',
    standalone: false,
    templateUrl: './auth-console.component.html',
    styleUrls: ['./auth-console.component.scss'],
})
export class AuthConsoleComponent {
    @Input() public currentAuthType: AuthType = AuthType.SIGN_UP
    @Output() public authPopupEvent: EventEmitter<AuthPopup> = new EventEmitter<AuthPopup>()
    protected attempts: number = 0
    constructor(
        private readonly signupService: SignupService,
        private readonly loginService: LoginService,
        private readonly router: Router,
        private readonly tokenStorageService: TokenStorageService,
        private readonly logger: CryptoTraderLoggerService,
    ) {
        this.logger.setContext(LoggerContext.Auth)
    }

    /**
     * Attempts to log in a user with input credentials.
     *
     * @param loginCredentials
     */
    protected attemptLogin(loginCredentials: LoginCredentials): void {
        if (loginCredentials.isFilledFields()) {
            this.login(loginCredentials.getRequest())
        } else {
            this.emitAuthPopup(AuthPopup.FILL_ALL_FIELDS)
        }
    }

    /**
     * Logs in a user with a provided request.
     *
     * @param loginRequest
     */
    private login(loginRequest: LoginRequest): void {
        this.loginService.login(loginRequest).subscribe({
            next: (authResponse: AuthResponse): void => {
                if (authResponse.authorized) {
                    this.saveToken(authResponse)
                    void this.router.navigate(['/portfolio'])
                } else {
                    this.emitAuthPopup(AuthPopup.INVALID_USERNAME_OR_PASSWORD)
                }
            },
            error: (err): void => {
                console.error('[HTTP][login] error:', err)
                this.emitAuthPopup(AuthPopup.INVALID_USERNAME_OR_PASSWORD)
            },
        })
    }

    /**
     * Saves authorization token from controller response.
     *
     * @param authResponse
     */
    private saveToken(authResponse: AuthResponse): void {
        if (authResponse.token) {
            this.tokenStorageService.setToken(authResponse.token)
        }
    }

    /**
     * Attempts to sign up a user with input credentials.
     *
     * @param signupCredentials
     */
    protected attemptSignup(signupCredentials: SignupCredentials): void {
        if (signupCredentials.getAnyIssue() === AuthPopup.NONE) {
            this.signup(signupCredentials.getSignupRequest())
        } else {
            this.emitAuthPopup(signupCredentials.getAnyIssue())
        }
    }

    /**
     * Signs up a user with a provided request.
     *
     * @param signupRequest
     */
    private signup(signupRequest: SignupRequest): void {
        this.logger.debug('Sending signup request via HTTP')
        this.signupService.signup(signupRequest).subscribe({
            next: (authResponse: AuthResponse): void => {
                if (authResponse.authorized) {
                    this.saveToken(authResponse)
                    void this.router.navigate(['/portfolio'])
                } else {
                    this.emitAuthPopup(AuthPopup.USERNAME_OR_EMAIL_EXISTS)
                }
            },
            error: (error): void => {
                if (error instanceof Error) {
                    this.logger.error('Signup error', error)
                } else {
                    this.logger.error('Signup error')
                }
                this.emitAuthPopup(AuthPopup.USERNAME_OR_EMAIL_EXISTS)
            },
        })
    }

    /**
     * Emits an authorization popup event to the parent component.
     *
     * @param authPopup
     */
    protected emitAuthPopup(authPopup: AuthPopup): void {
        this.authPopupEvent.emit(authPopup)
    }

    /**
     * Updates the current authentication type.
     *
     * @param authType
     */
    protected setAuthType(authType: AuthType): void {
        this.emitAuthPopup(AuthPopup.NONE)
        this.currentAuthType = authType
    }
    /**
     * Returns whether the current authentication type is signup.
     *
     * @returns {boolean} True if the current authentication type is signup,
     * false otherwise.
     */
    protected isSignupSection(): boolean {
        return this.currentAuthType === AuthType.SIGN_UP
    }

    /**
     * Returns whether the current authentication type is login.
     *
     * @returns {boolean} True if the current authentication type is login,
     * false otherwise.
     */
    protected isLoginSection(): boolean {
        return this.currentAuthType === AuthType.LOGIN
    }


    /**
     * Navigates to the home page.
     */
    protected navigateHome(): void {
        void this.router.navigate(['/'])
    }

    @HostListener('document:keydown.enter', ['$event'])
    protected onEnterKey(event: KeyboardEvent): void {
        event.preventDefault()
        if (this.isSignupSection()) {
            this.attemptSignup(new SignupCredentials())
        } else if (this.isLoginSection()) {
            this.attemptLogin(new LoginCredentials())
        }
    }

    protected readonly AuthPopup: typeof AuthPopup = AuthPopup
    protected readonly AuthType: typeof AuthType = AuthType
}
