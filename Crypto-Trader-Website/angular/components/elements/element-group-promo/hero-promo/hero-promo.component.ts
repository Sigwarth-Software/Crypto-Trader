import { Component } from '@angular/core';

import { ElementSize, TagType } from '@theoliverlear/angular-suite';
import { getStartedElementLink } from '@assets/element-link.assets';
import {
    transparentLogo,
    stockIcon,
    circleCheckmarkIcon,
    bookIcon,
} from '@assets/image.assets';

@Component({
    selector: 'hero-promo',
    standalone: false,
    templateUrl: './hero-promo.component.html',
    styleUrls: ['./hero-promo.component.scss'],
})
export class HeroPromoComponent {
    constructor() {}

    protected readonly TagType = TagType;
    protected readonly transparentLogo = transparentLogo;
    protected readonly stockIcon = stockIcon;
    protected readonly circleCheckmarkIcon = circleCheckmarkIcon;
    protected readonly bookIcon = bookIcon;
    protected readonly ElementSize = ElementSize;
    protected readonly getStartedElementLink = getStartedElementLink;
}
