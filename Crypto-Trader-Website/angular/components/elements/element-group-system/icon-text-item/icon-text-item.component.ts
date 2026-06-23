// icon-text-item.component.ts
import {Component, HostBinding, Input} from '@angular/core';
import { ImageAsset } from '@assets/image.assets';

@Component({
    selector: 'icon-text-item',
    standalone: false,
    templateUrl: './icon-text-item.component.html',
    styleUrls: ['./icon-text-item.component.scss'],
})
export class IconTextItemComponent {
    @HostBinding('class.use-invert-color') get invertColorClass(): boolean {
        return this.invertColor;
    }
    // TODO: Remove the non-null assertion.
    @Input() public icon!: ImageAsset;
    @Input() public text: string = '';
    @Input() public invertColor: boolean = false;
}
