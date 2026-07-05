// performance-arrow.component.ts
import { Component, HostBinding, Input } from '@angular/core';

import { TagType } from '@theoliverlear/angular-suite';
import { ImageAsset, upArrowIcon } from '@assets/image.assets';
import { PerformanceRating } from '@models/currency/types';

/** A component that displays an up/down arrow indicating the performance of
 *  a currency.
 */
@Component({
    selector: 'performance-arrow',
    templateUrl: './performance-arrow.component.html',
    styleUrls: ['./performance-arrow.component.scss'],
    standalone: false,
})
export class PerformanceArrowComponent {
    @Input() protected performance: PerformanceRating = {
        rating: 'neutral',
        changePercent: '0%',
    };
    @Input() protected includePercent: boolean = false;
    /** On positive performance, point upward.
     * @returns {boolean} true if currency is a positive performance, false otherwise.
     */
    @HostBinding('class.up') get isUp(): boolean {
        return this.performance && this.performance.rating === 'up';
    }
    /** On negative performance, point downward.
     * @returns {boolean} true if currency is a negative performance, false otherwise.
     */
    @HostBinding('class.down') get isDown(): boolean {
        return this.performance && this.performance.rating === 'down';
    }
    constructor() {}

    protected readonly upArrowIcon: ImageAsset = upArrowIcon;
    protected readonly TagType: typeof TagType = TagType;
}
