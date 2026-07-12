// performance-arrow.component.ts
import { Component, HostBinding, Input, OnChanges, SimpleChanges } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'
import { ImageAsset, upArrowIcon, whiteUpArrowIcon } from '@assets/image.assets'
import { PerformanceRating } from '@models/currency/types'
import { ImageColorVariation } from '@models/image/ImageColorVariation'

/** A component that displays an up/down arrow indicating the performance of
 *  a currency.
 */
@Component({
    selector: 'performance-arrow',
    templateUrl: './performance-arrow.component.html',
    styleUrls: ['./performance-arrow.component.scss'],
    standalone: false,
})
export class PerformanceArrowComponent implements OnChanges {
    @Input() public performance: PerformanceRating = {
        rating: 'neutral',
        changePercent: '0%',
    }
    @Input() public includePercent: boolean = false

    @Input() public imageColor: ImageColorVariation = ImageColorVariation.White

    protected imageAsset: ImageAsset = whiteUpArrowIcon

    public ngOnChanges(changes: SimpleChanges): void {
        if ('imageColor' in changes) {
            this.resolveImageAsset()
        }
    }

    private resolveImageAsset(): void {
        if (this.imageColor === ImageColorVariation.White) {
            this.imageAsset = whiteUpArrowIcon
        } else {
            this.imageAsset = upArrowIcon
        }
    }

    /** On positive performance, point upward.
     * @returns {boolean} true if currency is a positive performance, false otherwise.
     */
    @HostBinding('class.up') get isUp(): boolean {
        return this.performance && this.performance.rating === 'up'
    }
    /** On negative performance, point downward.
     * @returns {boolean} true if currency is a negative performance, false otherwise.
     */
    @HostBinding('class.down') get isDown(): boolean {
        return this.performance && this.performance.rating === 'down'
    }
    constructor() {}

    protected readonly upArrowIcon: ImageAsset = whiteUpArrowIcon
    protected readonly TagType: typeof TagType = TagType
}
