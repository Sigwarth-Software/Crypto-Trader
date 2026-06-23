// home-anchor.component.ts
import { Component } from '@angular/core';

import { homeElementLink, navBarHomeLink } from '@assets/element-link.assets';
import { transparentLogo } from '@assets/image.assets';

@Component({
    selector: 'home-anchor',
    templateUrl: './home-anchor.component.html',
    styleUrls: ['./home-anchor.component.scss'],
    standalone: false,
})
export class HomeAnchorComponent {
    constructor() {}

    protected readonly transparentLogo = transparentLogo;
    protected readonly homeElementLink = homeElementLink;
}
