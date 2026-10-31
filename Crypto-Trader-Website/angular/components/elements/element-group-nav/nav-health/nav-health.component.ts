// nav-health.component.ts
import { Component } from '@angular/core';
import { whiteHealthModuleIcon } from '@assets/image.assets'
import { healthElementLink } from '@assets/element-link.assets'

@Component({
    selector: 'nav-health',
    templateUrl: './nav-health.component.html',
    styleUrls: ['./nav-health.component.scss'],
    standalone: false
})
export class NavHealthComponent {
    constructor() {

    }

    protected readonly whiteHealthModuleIcon = whiteHealthModuleIcon
    protected readonly healthElementLink = healthElementLink
}
