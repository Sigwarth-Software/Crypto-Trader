// auth-console-signup-section.component.ts
import { Component, EventEmitter, OnInit, Output } from '@angular/core'

import { AuthPopup, ButtonText, ElementSize } from '@theoliverlear/angular-suite'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { SignupCredentials } from '@models/auth/SignupCredentials'

import { AuthInputType } from '../auth-input/models/AuthInputType'

/** A section for signup up in the auth console.
 *
 */
@Component({
    selector: 'auth-console-signup-section',
    standalone: false,
    templateUrl: './auth-console-signup-section.component.html',
    styleUrls: ['./auth-console-signup-section.component.scss'],
})
export class AuthConsoleSignupSectionComponent implements OnInit {
    protected signupCredentials: SignupCredentials = new SignupCredentials()
    @Output() public signupButtonClicked: EventEmitter<SignupCredentials> =
        new EventEmitter<SignupCredentials>()
    @Output() public authPopupEvent: EventEmitter<AuthPopup> = new EventEmitter<AuthPopup>()
    constructor(private readonly log: CryptoTraderLoggerService) {}

    /**
     * On initialization, set the logger context.
     */
    public ngOnInit(): void {
        this.log.setContext('AuthConsoleSignup')
    }

    /** Emits an authorization popup event to the parent component.
     *
     * @param authPopup
     */
    protected emitAuthPopup(authPopup: AuthPopup): void {
        this.authPopupEvent.emit(authPopup)
    }

    /** Updates the agreed terms checkbox.
     *
     * @param agree
     */
    protected updateAgreedTerms(agree: string): void {
        this.signupCredentials.agreedTerms = Boolean(agree)
    }

    /** Updates the email input.
     *
     * @param email
     */
    protected updateEmail(email: string): void {
        this.signupCredentials.email = email
        this.emitPossibleInvalidEmail()
    }

    /** Emits an authorization popup event if the email is invalid.
     *
     * @private
     */
    private emitPossibleInvalidEmail(): void {
        if (!this.signupCredentials.isValidEmail()) {
            this.log.warn('Invalid email format detected during signup')
            this.emitAuthPopup(AuthPopup.INVALID_EMAIL)
        } else {
            this.emitAuthPopup(this.signupCredentials.getAnyTypingIssue())
        }
    }

    /** Updates the password input.
     *
     * @param password
     */
    protected updatePassword(password: string): void {
        this.signupCredentials.password = password
        this.emitPossibleMismatch()
    }

    /** Emits an authorization popup event if the passwords don't match.
     *
     * @private
     */
    private emitPossibleMismatch(): void {
        if (!this.signupCredentials.isPasswordMatch()) {
            this.log.warn('Passwords do not match during signup')
            this.emitAuthPopup(AuthPopup.PASSWORDS_DONT_MATCH)
        } else {
            this.emitAuthPopup(this.signupCredentials.getAnyTypingIssue())
        }
    }

    /** Updates the confirm password input.
     *
     * @param confirmPassword
     */
    protected updateConfirmPassword(confirmPassword: string): void {
        this.signupCredentials.confirmPassword = confirmPassword
        this.emitPossibleMismatch()
    }
    /** Emits all signup credentials to the parent component.
     *
     */
    protected emitFields(): void {
        this.log.info('Signup button clicked, emitting credentials')
        this.signupButtonClicked.emit(this.signupCredentials)
    }

    protected readonly AuthInputType: typeof AuthInputType = AuthInputType
    protected readonly ElementSize: typeof ElementSize = ElementSize
    protected readonly ButtonText: typeof ButtonText = ButtonText
}
