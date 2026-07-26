// portfolio-section-arrow.component.ts
import { Component, Input } from '@angular/core'

import { ArrowDirection } from '@components/elements/element-group-system/arrow/models/ArrowDirection'
import { PortfolioSectionArrowType } from './models/PortfolioSectionArrowType'

/**
 * A component that displays an arrow for navigating between portfolio
 * sections.
 */
@Component({
    selector: 'portfolio-section-arrow',
    templateUrl: './portfolio-section-arrow.component.html',
    styleUrls: ['./portfolio-section-arrow.component.scss'],
    standalone: false,
})
export class PortfolioSectionArrowComponent {
    @Input() public sectionType: PortfolioSectionArrowType
    constructor() {}

    protected getArrowDirection(): ArrowDirection {
        switch (this.sectionType) {
            case PortfolioSectionArrowType.REPORT:
                return ArrowDirection.Left
            case PortfolioSectionArrowType.MANAGE:
                return ArrowDirection.Right
            default:
                return ArrowDirection.Right
        }
    }
}
