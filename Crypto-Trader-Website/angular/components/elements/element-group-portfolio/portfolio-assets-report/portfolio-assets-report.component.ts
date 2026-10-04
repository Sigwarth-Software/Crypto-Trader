// portfolio-assets-report.component.ts
import { Component, Input } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'
import { Portfolio } from '@models/portfolio/types'

/**
 * A report of the assets in a portfolio.
 */
@Component({
    selector: 'portfolio-assets-report',
    templateUrl: './portfolio-assets-report.component.html',
    styleUrls: ['./portfolio-assets-report.component.scss'],
    standalone: false,
})
export class PortfolioAssetsReportComponent {
    @Input() public portfolio: Portfolio
    constructor() {}

    protected hasCurrencies(): boolean {
        return this.portfolio.assets.length > 0
    }

    protected readonly TagType: typeof TagType = TagType
}
