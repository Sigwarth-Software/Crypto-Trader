// icon-text-item.component.ts
import {Component, Input} from '@angular/core';
import { ImageAsset } from '@assets/image.assets';

@Component({
    selector: 'icon-text-item',
    standalone: false,
    templateUrl: './icon-text-item.component.html',
    styleUrls: ['./icon-text-item.component.scss'],
})
export class IconTextItemComponent {
    // TODO: Remove the non-null assertion.
    @Input() public icon!: ImageAsset;
    @Input() public text: string = '';
}
