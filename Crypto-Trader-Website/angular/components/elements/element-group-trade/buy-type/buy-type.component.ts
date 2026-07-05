// buy-type.component.ts
import {
    Component,
    EventEmitter,
    HostListener,
    Input,
    Output,
} from '@angular/core';

import { ElementSize } from '@theoliverlear/angular-suite';
import { coinIcon, dollarIcon, ImageAsset } from '@assets/image.assets';

import { BuyType } from './models/BuyType';

@Component({
    selector: 'buy-type',
    templateUrl: './buy-type.component.html',
    styleUrls: ['./buy-type.component.scss'],
    standalone: false,
})
export class BuyTypeComponent {
    @Input() public buyType: BuyType;
    @Output() public buyTypeSelectedEvent: EventEmitter<BuyType> = new EventEmitter<BuyType>();
    constructor() {}

    @HostListener('click')
    public onClick(): void {
        this.buyTypeSelectedEvent.emit(this.buyType);
    }

    public getImageAsset(): ImageAsset {
        if (this.buyType === BuyType.DOLLARS) {
            return dollarIcon;
        } else {
            return coinIcon;
        }
    }

    protected readonly ElementSize: typeof ElementSize = ElementSize;
}
