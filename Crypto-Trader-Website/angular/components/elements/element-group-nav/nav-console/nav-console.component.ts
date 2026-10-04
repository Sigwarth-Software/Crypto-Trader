// nav-console.component.ts
import { Component } from '@angular/core'

import { consoleElementLink } from '@assets/element-link.assets'
import { consoleIcon, ImageAsset } from '@assets/image.assets'
import { ElementLink } from '@theoliverlear/angular-suite'

/**
 * A console navigation element that links to the console page.
 */
@Component({
    selector: 'nav-console',
    templateUrl: './nav-console.component.html',
    styleUrls: ['./nav-console.component.scss'],
    standalone: false,
})
export class NavConsoleComponent {
    constructor() {}

    protected readonly consoleElementLink: ElementLink = consoleElementLink
    protected readonly consoleIcon: ImageAsset = consoleIcon
}
