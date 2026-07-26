// transparency-promo.component.ts
import { Component } from '@angular/core'
import { TagType } from '@theoliverlear/angular-suite'
import { circleCheckmarkIcon, bookIcon, electricPlugIcon, ImageAsset } from '@assets/image.assets'
import { homeEngineCodeWindow } from '@assets/code-window.assets'
import { CodeWindow } from '@models/promo/types'

/**
 * A component for displaying a transparency promo.
 */
@Component({
    selector: 'transparency-promo',
    standalone: false,
    templateUrl: './transparency-promo.component.html',
    styleUrls: ['./transparency-promo.component.scss'],
})
export class TransparencyPromoComponent {
    protected readonly TagType: typeof TagType = TagType
    protected readonly circleCheckmarkIcon: ImageAsset = circleCheckmarkIcon
    protected readonly bookIcon: ImageAsset = bookIcon
    protected readonly electricPlugIcon: ImageAsset = electricPlugIcon
    protected readonly homeEngineCodeWindow: CodeWindow = homeEngineCodeWindow
}
