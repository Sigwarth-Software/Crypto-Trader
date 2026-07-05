// asset-kill-switch.component.ts
import { Component, HostBinding, HostListener, Input } from '@angular/core';

import { electricPlugIcon, ImageAsset } from '@assets/image.assets';

/** A component that toggles the kill switch for an asset.
 *
 */
@Component({
    selector: 'asset-kill-switch',
    templateUrl: './asset-kill-switch.component.html',
    styleUrls: ['./asset-kill-switch.component.scss'],
    standalone: false,
})
export class AssetKillSwitchComponent {
    @Input() public isKilled: boolean = false;
    @HostBinding('class.is-killed') protected get isKilledClass(): boolean {
        return this.isKilled;
    }
    constructor() {}

    /** Toggles the kill switch for the asset when the component is clicked.
     *
     */
    @HostListener('click')
    public onClick(): void {
        this.toggleKillSwitch();
    }

    protected toggleKillSwitch(): void {
        this.isKilled = !this.isKilled;
    }

    protected readonly electricPlugIcon: ImageAsset = electricPlugIcon;
}
