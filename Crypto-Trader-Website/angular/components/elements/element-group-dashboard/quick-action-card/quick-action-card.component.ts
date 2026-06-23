// quick-action-card.component.ts
import { Component, Input } from '@angular/core';

import { TagType } from '@theoliverlear/angular-suite';
import { ImageAsset } from '@assets/image.assets';

@Component({
    selector: 'quick-action-card',
    standalone: false,
    templateUrl: './quick-action-card.component.html',
    styleUrls: ['./quick-action-card.component.scss'],
})
export class QuickActionCardComponent {
    @Input() label: string = '';
    @Input() description: string = '';
    @Input() route: string = '/';
    // TODO: Remove the non-null assertion.
    @Input() icon!: ImageAsset;
    @Input() isConsole: boolean = false;

    protected readonly TagType: typeof TagType = TagType;
}
