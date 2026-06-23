import { Component } from '@angular/core';

import { navBarAccountLink } from '@assets/element-link.assets';
import { ElementLink } from '@theoliverlear/angular-suite';

/** Profile picture in the navigation bar for account access.
 *
 */
@Component({
    selector: 'nav-bar-profile-picture',
    standalone: false,
    templateUrl: './nav-bar-profile-picture.component.html',
    styleUrls: ['./nav-bar-profile-picture.component.scss'],
})
export class NavBarProfilePictureComponent {
    constructor() {}

    protected readonly navBarAccountLink: ElementLink = navBarAccountLink;
}
