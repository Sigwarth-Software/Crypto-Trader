// statistics.component.ts
import { AfterViewInit, Component, OnInit } from '@angular/core'

import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
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

/**
 * The page displaying statistics about user's Crypto Trader performance.
 */
@Component({
    selector: 'statistics',
    templateUrl: './statistics.component.html',
    styleUrls: ['./statistics.component.scss'],
    standalone: false,
})
export class StatisticsComponent implements OnInit, AfterViewInit {
    protected chartConfig: ChartConfig = {
        ...defaultChartConfig,
        dimensions: { ...defaultChartConfig.dimensions },
    }

    protected portfolioHistories: PortfolioHistory[] = []
    protected chartData: HistoryPoint[] = []

    constructor(
        private readonly log: CryptoTraderLoggerService,
        private readonly portfolioHistoryService: PortfolioHistoryService,
        private readonly rangedPortfolioHistoryService: RangedPortfolioHistoryService,
        private readonly multiDataLoaderService: MultiDataLoaderService
    ) {}

    /**
     * On init, set the logger context.
     */
    public ngOnInit(): void {
        this.log.setContext(LoggerContext.Statistics)
        this.multiDataLoaderService.setLoading('portfolio-history', true)
    }

    protected isLoadingPortfolioHistory(): boolean {
        return this.multiDataLoaderService.getLoading('portfolio-history')
    }

    public ngAfterViewInit(): void {
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
            .subscribe((portfolioHistories): void => {
                this.portfolioHistories = portfolioHistories
                this.log.log(`Fetched ${portfolioHistories.length} portfolio history entries.`)
                this.chartData = portfolioHistories.map((portfolioHistory): HistoryPoint => {
                    return {
                        date: new Date(portfolioHistory.lastUpdated),
                        value: portfolioHistory.totalWorth,
                    }
                })
                this.multiDataLoaderService.setLoading('portfolio-history', false)
            })
    }

    protected readonly ElementSize = ElementSize
    protected readonly LoadingWheelColorScheme = LoadingWheelColorScheme
    protected readonly statisticsPageTitleStripe = statisticsPageTitleStripe
}
