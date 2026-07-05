// auth-console-tab-section.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core'

import { AuthPopup, AuthType } from '@theoliverlear/angular-suite'

/** A section that contains tabs for switching between auth types.
 *
 */
@Component({
    selector: 'auth-console-tab-section',
    standalone: false,
    templateUrl: './auth-console-tab-section.component.html',
    styleUrls: ['./auth-console-tab-section.component.scss'],
})
export class AuthConsoleTabSectionComponent {
    @Input() public currentAuthType: AuthType = AuthType.SIGN_UP
    @Output() public authTypeClicked: EventEmitter<AuthType> = new EventEmitter<AuthType>()
    constructor() {}
    /** Emits the auth type when clicked.
     *
     * @param authType
     */
    protected emitAuthTypeClicked(authType: AuthType): void {
        this.authTypeClicked.emit(authType)
    }
    // TODO: This method is duplicated. Refactor.
    /** Emits the auth type when clicked.
     *
     * @param authType
     */
    protected setAuthType(authType: AuthType): void {
        this.emitAuthTypeClicked(authType)
    }

    protected readonly AuthType: typeof AuthType = AuthType
    protected readonly AuthPopup: typeof AuthPopup = AuthPopup
}
