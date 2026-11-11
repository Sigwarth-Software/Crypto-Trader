// statistics.component.ts
import { AfterViewInit, Component, OnInit } from '@angular/core'

import { LoggerContext } from '@models/logging/LoggerContext'
import type { ChartConfig } from '@models/chart/types'
import { defaultChartConfig } from '@assets/chart.assets'
import {
    PortfolioHistoryService
} from '@http/portfolio-history/portfolio-history.service'
import { PortfolioHistory } from '@models/portfolio/types'
import { HistoryPoint } from '@models/currency/types'
import {
    RangedPortfolioHistoryService
} from '@http/portfolio-history/ranged-portfolio-history.service'
import { ElementSize } from '@theoliverlear/angular-suite'
import {
    LoadingWheelColorScheme
} from '@components/elements/element-group-system/loading-wheel/models/LoadingWheelColorScheme'
import { MultiDataLoaderService } from '@ui/multi-data-loader.service'
import { finalize } from 'rxjs'
import { statisticsPageTitleStripe } from '@assets/page-title-stripe.assets'
import {
    BaseLoggingElementComponent
} from '@components/elements/element-group-system/base-logging-element/base-logging-element.component'
import {
    PageTitleStripe
} from '@components/elements/element-group-system/page-title-stripe/models/PageTitleStripe'

/**
 * The page displaying statistics about user's Crypto Trader performance.
 */
@Component({
    selector: 'statistics',
    templateUrl: './statistics.component.html',
    styleUrls: ['./statistics.component.scss'],
    standalone: false,
})
export class StatisticsComponent extends BaseLoggingElementComponent implements OnInit, AfterViewInit {
    protected chartConfig: ChartConfig = {
        ...defaultChartConfig,
        dimensions: { ...defaultChartConfig.dimensions },
    }

    protected portfolioHistories: PortfolioHistory[] = []
    protected chartData: HistoryPoint[] = []

    constructor(
        private readonly portfolioHistoryService: PortfolioHistoryService,
        private readonly rangedPortfolioHistoryService: RangedPortfolioHistoryService,
        private readonly multiDataLoaderService: MultiDataLoaderService
    ) {
        super()
        this.loggerContext = LoggerContext.Statistics
    }

    /**
     * On init, set the logger context.
     */
    public ngOnInit(): void {
        super.ngOnInit()
        this.multiDataLoaderService.setLoading('portfolio-history', true)
    }

    protected isLoadingPortfolioHistory(): boolean {
        return this.multiDataLoaderService.getLoading('portfolio-history')
    }

    /**
     * After view initialization, fetch the portfolio history for the graph.
     */
    public ngAfterViewInit(): void {
        this.log.log('Loading view for statistics.')
        const today: Date = new Date()
        const sevenDaysAgo: Date = new Date(today)
        sevenDaysAgo.setDate(today.getDate() - 7)
        this.rangedPortfolioHistoryService
            .getRangedPortfolioHistory({
                startDate: sevenDaysAgo.toISOString().replace('Z', ''),
                endDate: today.toISOString().replace('Z', ''),
            })
            .pipe(
                finalize((): void => {
                    this.multiDataLoaderService.setLoading('portfolio-history', false)
                }),
            )
            .subscribe((portfolioHistories: PortfolioHistory[]): void => {
                this.portfolioHistories = portfolioHistories
                this.log.log(`Fetched ${portfolioHistories.length} portfolio history entries.`)
                this.chartData = portfolioHistories.map((portfolioHistory: PortfolioHistory): HistoryPoint => {
                    return {
                        date: new Date(portfolioHistory.lastUpdated),
                        value: portfolioHistory.totalWorth,
                    }
                })
                this.multiDataLoaderService.setLoading('portfolio-history', false)
            })
    }

    protected readonly ElementSize: typeof ElementSize = ElementSize
    protected readonly LoadingWheelColorScheme: typeof LoadingWheelColorScheme = LoadingWheelColorScheme
    protected readonly statisticsPageTitleStripe: PageTitleStripe = statisticsPageTitleStripe
}
