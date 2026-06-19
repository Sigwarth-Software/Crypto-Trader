// auth-console-tab.component.ts
import { Component, EventEmitter, HostBinding, HostListener, Input, Output } from '@angular/core'

import { AuthType, TagType } from '@theoliverlear/angular-suite'

/** A tab that can be clicked to switch between auth types.
 *
 */
@Component({
    selector: 'auth-console-tab',
    standalone: false,
    templateUrl: './auth-console-tab.component.html',
    styleUrls: ['./auth-console-tab.component.scss'],
})
export class AuthConsoleTabComponent {
    @Input() public authType: AuthType
    @Input() public isActive: boolean = false
    @Output() public authTabClicked: EventEmitter<AuthType> = new EventEmitter<AuthType>()
    constructor() {}

    /**
     * Apply active class when the tab is active.
     */
    @HostBinding('class.active')
    get activeClass(): boolean {
        return this.isActive
    }

    /** Emits the auth type when clicked.
     *
     */
    protected emitAuthTabClicked(): void {
        this.authTabClicked.emit(this.authType)
    }
    /** Emits the auth type when clicked.
     *
     */
    @HostListener('click')
    protected onClick(): void {
        this.emitAuthTabClicked()
    }

    protected readonly TagType: typeof TagType = TagType
}
