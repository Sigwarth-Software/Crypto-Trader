import { Component, OnInit } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'
import { defaultPortfolio } from '@assets/portfolio.assets'
import { PortfolioService } from '@http/portfolio/portfolio.service'
import { Portfolio } from '@models/portfolio/types'

import { PortfolioSectionArrowType } from '../../elements/element-group-portfolio/portfolio-section-arrow/models/PortfolioSectionArrowType'
import {LoggerContext} from "@models/logging/LoggerContext"
import { portfolioPageTitleStripe } from '@assets/page-title-stripe.assets'
import {
    PageTitleStripe
} from '@components/elements/element-group-system/page-title-stripe/models/PageTitleStripe'
import {
    BaseLoggingElementComponent
} from '@components/elements/element-group-system/base-logging-element/base-logging-element.component'

@Component({
    selector: 'portfolio',
    standalone: false,
    templateUrl: './portfolio.component.html',
    styleUrls: ['./portfolio.component.scss'],
})
export class PortfolioComponent extends BaseLoggingElementComponent implements OnInit {
    protected portfolio: Portfolio = defaultPortfolio

    constructor(
        private readonly portfolioService: PortfolioService,
    ) {
        super()
        this.loggerContext = LoggerContext.Portfolio
    }

    public ngOnInit(): void {
        this.log.debug('Fetching portfolio')
        this.portfolioService.getPortfolio().subscribe({
            next: (data: Portfolio): void => {
                this.log.info('Portfolio fetched successfully')
                this.portfolio = data
            },
            error: (error: unknown): void => {
                const errorMessage: string = error instanceof Error ? error.message : 'Unknown error'
                this.log.error('Failed to fetch portfolio', errorMessage)
            },
        })
    }

    protected hasCurrencies(): boolean {
        return this.portfolio.assets.length > 0
    }

    protected readonly TagType: typeof TagType = TagType
    protected readonly PortfolioSectionArrowType: typeof PortfolioSectionArrowType = PortfolioSectionArrowType
    protected readonly portfolioPageTitleStripe: PageTitleStripe = portfolioPageTitleStripe
}
