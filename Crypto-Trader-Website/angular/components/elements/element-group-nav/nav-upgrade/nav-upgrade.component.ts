import { Component } from '@angular/core'

import { upgradeElementLink } from '@assets/element-link.assets'
import { ElementLink } from '@theoliverlear/angular-suite'

/**
 * A navigation element that links to the upgrade page.
 */
@Component({
    selector: 'nav-upgrade',
    templateUrl: './nav-upgrade.component.html',
    styleUrls: ['./nav-upgrade.component.scss'],
    standalone: false,
})
export class NavUpgradeComponent {
    constructor() {}

    protected readonly upgradeElementLink: ElementLink = upgradeElementLink
}
