// arrow.component.ts
import { Component, HostBinding, Input } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'
import { PossibleString } from '@models/types'

import { ArrowDirection } from './models/ArrowDirection'

/**
 * Base arrow icon that points a certain direction.
 */
@Component({
    selector: 'arrow',
    templateUrl: './arrow.component.html',
    styleUrls: ['./arrow.component.scss'],
    standalone: false,
})
export class ArrowComponent {
    @Input() public direction: ArrowDirection = ArrowDirection.Right
    @Input() public text: PossibleString
    /** Binds the direction to the host element's class.
     *  @returns The direction class as a string.
     */
    @HostBinding('class')
    public get hostClasses(): string {
        return this.direction
    }
    constructor() {}

    protected readonly TagType: typeof TagType = TagType
}
