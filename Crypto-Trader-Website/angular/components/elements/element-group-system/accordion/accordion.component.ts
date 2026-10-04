// accordion.component.ts
import { Component, Input } from '@angular/core'

/**
 * A popout accordion with hidden content.
 */
@Component({
    selector: 'accordion',
    templateUrl: './accordion.component.html',
    styleUrls: ['./accordion.component.scss'],
    standalone: false,
})
export class AccordionComponent {
    @Input() public title: string = ''
    @Input() public description: string = ''
    constructor() {}
}
