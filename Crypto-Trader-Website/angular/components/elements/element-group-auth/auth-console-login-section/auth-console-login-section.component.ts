// auth-console-login-section.component.ts
import { Component, EventEmitter, OnInit, Output } from '@angular/core'

import { ButtonText, ElementSize } from '@theoliverlear/angular-suite'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { LoginCredentials } from '@models/auth/LoginCredentials'

import { AuthInputType } from '../auth-input/models/AuthInputType'

/** A section for login in the auth console.
 *
 */
@Component({
    selector: 'auth-console-login-section',
    standalone: false,
    templateUrl: './auth-console-login-section.component.html',
    styleUrls: ['./auth-console-login-section.component.scss'],
})
export class AuthConsoleLoginSectionComponent implements OnInit {
    protected loginCredentials: LoginCredentials = new LoginCredentials()
    @Output() protected loginButtonClicked: EventEmitter<LoginCredentials> =
        new EventEmitter<LoginCredentials>()
    constructor(private readonly log: CryptoTraderLoggerService) {}

    /**
     * On initialization, set the logger context.
     */
    public ngOnInit(): void {
        this.log.setContext('AuthConsoleLogin')
    }

    protected emitFields(): void {
        this.log.info('Login button clicked, emitting credentials')
        this.loginButtonClicked.emit(this.loginCredentials)
    }

    protected updateEmail(email: string): void {
        this.loginCredentials.email = email
    }

    protected updatePassword(password: string): void {
        this.loginCredentials.password = password
    }

    protected readonly AuthInputType: typeof AuthInputType = AuthInputType
    protected readonly ElementSize: typeof ElementSize = ElementSize
    protected readonly ButtonText: typeof ButtonText = ButtonText
}
