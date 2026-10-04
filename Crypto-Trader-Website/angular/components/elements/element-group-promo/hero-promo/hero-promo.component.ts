import { Component } from '@angular/core'

import { ElementSize, TagType, TextElementLink } from '@theoliverlear/angular-suite'
import { getStartedElementLink } from '@assets/element-link.assets'
import {
    transparentLogo,
    stockIcon,
    circleCheckmarkIcon,
    bookIcon,
    ImageAsset,
} from '@assets/image.assets'

/**
 * A component for displaying a hero promo.
 */
@Component({
    selector: 'hero-promo',
    standalone: false,
    templateUrl: './hero-promo.component.html',
    styleUrls: ['./hero-promo.component.scss'],
})
export class HeroPromoComponent {
    constructor() {}

    protected readonly TagType: typeof TagType = TagType
    protected readonly transparentLogo: ImageAsset = transparentLogo
    protected readonly stockIcon: ImageAsset = stockIcon
    protected readonly circleCheckmarkIcon: ImageAsset = circleCheckmarkIcon
    protected readonly bookIcon: ImageAsset = bookIcon
    protected readonly ElementSize: typeof ElementSize = ElementSize
    protected readonly getStartedElementLink: TextElementLink = getStartedElementLink
}
