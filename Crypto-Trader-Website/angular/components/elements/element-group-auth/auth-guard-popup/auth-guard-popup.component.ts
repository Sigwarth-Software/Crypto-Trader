// auth-guard-popup.component.ts
import { Component } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'

/** A validation popup for authentication.
 *
 */
@Component({
    selector: 'auth-guard-popup',
    templateUrl: './auth-guard-popup.component.html',
    styleUrls: ['./auth-guard-popup.component.scss'],
    standalone: false,
})
export class AuthGuardPopupComponent {
    constructor() {}

    protected readonly TagType: typeof TagType = TagType
}
