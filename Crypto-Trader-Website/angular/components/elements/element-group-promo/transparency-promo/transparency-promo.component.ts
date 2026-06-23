// transparency-promo.component.ts
import { Component } from '@angular/core';
import { TagType } from '@theoliverlear/angular-suite';
import {
    circleCheckmarkIcon,
    bookIcon,
    electricPlugIcon,
} from '@assets/image.assets';
import { homeEngineCodeWindow } from '@assets/code-window.assets';

@Component({
    selector: 'transparency-promo',
    standalone: false,
    templateUrl: './transparency-promo.component.html',
    styleUrls: ['./transparency-promo.component.scss'],
})
export class TransparencyPromoComponent {
    protected readonly TagType: typeof TagType = TagType;
    protected readonly circleCheckmarkIcon = circleCheckmarkIcon;
    protected readonly bookIcon = bookIcon;
    protected readonly electricPlugIcon = electricPlugIcon;


    protected readonly homeEngineCodeWindow = homeEngineCodeWindow;
}
