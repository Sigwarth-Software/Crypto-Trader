// nav-console.component.ts
import { Component } from '@angular/core';

import { consoleElementLink } from '@assets/element-link.assets';
import { consoleIcon } from '@assets/image.assets';

@Component({
    selector: 'nav-console',
    templateUrl: './nav-console.component.html',
    styleUrls: ['./nav-console.component.scss'],
    standalone: false,
})
export class NavConsoleComponent {
    constructor() {}

    protected readonly consoleElementLink = consoleElementLink;
    protected readonly consoleIcon = consoleIcon;
}
