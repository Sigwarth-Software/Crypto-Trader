// home-anchor.component.ts
import { Component } from '@angular/core'

import { homeElementLink } from '@assets/element-link.assets'
import { ImageAsset, transparentLogo } from '@assets/image.assets'
import { ElementLink } from '@theoliverlear/angular-suite'

/**
 * A component that displays an anchor to the home page.
 */
@Component({
    selector: 'home-anchor',
    templateUrl: './home-anchor.component.html',
    styleUrls: ['./home-anchor.component.scss'],
    standalone: false,
})
export class HomeAnchorComponent {
    constructor() {}

    protected readonly transparentLogo: ImageAsset = transparentLogo
    protected readonly homeElementLink: ElementLink = homeElementLink
}
