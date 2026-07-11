import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import {
    Chart,
    registerables,
    ChartData,
    ChartOptions,
} from 'chart.js';
// TODO: This could be D3 instead.
Chart.register(...registerables);
import { TagType } from '@theoliverlear/angular-suite';
import {
    coinIcon,
    walletIcon,
    circleCheckmarkIcon,
    exchangeArrowsIcon,
    stockScaleIcon,
    consoleIcon,
    profileIcon,
    type ImageAsset,
} from '@assets/image.assets';
import { defaultPortfolio } from '@assets/portfolio.assets';
import { PortfolioService } from '@http/portfolio/portfolio.service';
import { DisplayCurrenciesService } from '@http/currency/display-currencies.service';
import { CurrencyDayPerformanceService } from '@http/currency/currency-day-performance.service';
import { AllTradeEventsService } from '@http/trader/all-trade-events.service';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import { CurrencyFormatterService } from '@ui/currency-formatter.service';
import { SharesFormatterService } from '@ui/shares-formatter.service';
import { TimeFormatterService } from '@ui/time-formatter.service';
import { Portfolio, PortfolioAsset } from '@models/portfolio/types';
import { DisplayCurrency, PerformanceRating } from '@models/currency/types';
import { TradeEvent } from '@models/trader/types';
import {LoggerContext} from "@models/logging/LoggerContext";
import { dashboardPageTitleStripe } from '@assets/page-title-stripe.assets'

export interface DashboardCard {
    label: string;
    description: string;
    route: string;
    icon: ImageAsset;
}

@Component({
    selector: 'dashboard',
    standalone: false,
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
    protected portfolio: Portfolio = defaultPortfolio
    protected isFetching: boolean = true
    protected currencies: DisplayCurrency[] = []
    protected isFetchingCurrencies: boolean = true
    protected currencyPerformanceMap: Map<string, PerformanceRating> = new Map()
    protected recentTrades: TradeEvent[] = []
    protected isFetchingTrades: boolean = true

    protected allocationChartData: ChartData<'doughnut'> = {
        labels: [],
        datasets: [{ data: [], backgroundColor: [] }],
    }

    // TODO: Move partial to assets. Load partial with factory.
    protected allocationChartOptions: ChartOptions<'doughnut'> = {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    color: '#a4dbc2',
                    padding: 14,
                    usePointStyle: true,
                    pointStyle: 'circle',
                    pointStyleWidth: 10,
                    font: { size: 12, family: 'inherit' },
                },
            },
            tooltip: {
                backgroundColor: 'rgba(7, 61, 68, 0.95)',
                titleColor: '#c0ecd7',
                bodyColor: '#ffffff',
                borderColor: 'rgba(62, 185, 141, 0.4)',
                borderWidth: 1,
                cornerRadius: 6,
                padding: 10,
                callbacks: {
                    label: (ctx): string => {
                        const val = ctx.parsed
                        return ` ${ctx.label}: $${val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                    },
                },
            },
        },
    }

    protected assetBarChartData: ChartData<'bar'> = {
        labels: [],
        datasets: [{ data: [], backgroundColor: [] }],
    }
    // TODO: Move to assets.
    protected assetBarChartOptions: ChartOptions<'bar'> = {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: 'y',
        plugins: {
            legend: { display: false },
            tooltip: {
                backgroundColor: 'rgba(7, 61, 68, 0.95)',
                titleColor: '#c0ecd7',
                bodyColor: '#ffffff',
                borderColor: 'rgba(62, 185, 141, 0.4)',
                borderWidth: 1,
                cornerRadius: 6,
                padding: 10,
                callbacks: {
                    label: (ctx): string => {
                        const val = ctx.parsed.x ?? 0
                        return ` $${val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                    },
                },
            },
        },
        scales: {
            x: {
                ticks: {
                    color: '#a4dbc2',
                    callback: (value): string => `$${Number(value).toLocaleString()}`,
                    font: { size: 11 },
                },
                grid: {
                    color: 'rgba(62, 185, 141, 0.08)',
                    // @ts-ignore - drawTicks exists at runtime
                    drawTicks: false,
                },
                border: { display: false },
            },
            y: {
                ticks: { color: '#c0ecd7', font: { size: 12 } },
                grid: { display: false },
                border: { display: false },
            },
        },
    }

    // TODO: Move to assets.
    private readonly chartPalette: string[] = [
        '#3eb98d',
        '#26929d',
        '#f7931a',
        '#6480eb',
        '#13899d',
        '#6fc164',
        '#375f9e',
        '#e85d5d',
        '#3c9e7a',
        '#073d44',
        '#ffe17b',
        '#a4dbc2',
    ]
    // TODO: Move to assets.
    protected readonly cards: DashboardCard[] = [
        {
            label: 'Currencies',
            description: 'Browse live crypto prices and market data.',
            route: '/currencies',
            icon: coinIcon,
        },
        {
            label: 'Portfolio',
            description: 'View your assets, balances, and total worth.',
            route: '/portfolio',
            icon: walletIcon,
        },
        {
            label: 'Trader',
            description: 'Browse and filter your full trade history.',
            route: '/trader',
            icon: circleCheckmarkIcon,
        },
        {
            label: 'Trade',
            description: 'Execute buy and sell orders instantly.',
            route: '/trade',
            icon: exchangeArrowsIcon,
        },
        {
            label: 'Statistics',
            description: 'Analyze performance trends and insights.',
            route: '/statistics',
            icon: stockScaleIcon,
        },
        {
            label: 'Console',
            description: 'Run advanced commands via the terminal.',
            route: '/console',
            icon: consoleIcon,
        },
        {
            label: 'Account',
            description: 'Manage your profile and preferences.',
            route: '/account',
            icon: profileIcon,
        },
    ]

    constructor(
        private readonly portfolioService: PortfolioService,
        private readonly currencyFormatter: CurrencyFormatterService,
        private readonly sharesFormatter: SharesFormatterService,
        private readonly timeFormatter: TimeFormatterService,
        private readonly displayCurrenciesService: DisplayCurrenciesService,
        private readonly currencyDayPerformanceService: CurrencyDayPerformanceService,
        private readonly allTradeEventsService: AllTradeEventsService,
        private readonly router: Router,
        private readonly log: CryptoTraderLoggerService,
    ) {}

    public ngOnInit(): void {
        this.log.setContext(LoggerContext.Dashboard)

        this.log.debug('Fetching portfolio...')
        this.portfolioService.getPortfolio().subscribe({
            next: (portfolio: Portfolio): void => {
                this.log.info('Portfolio fetched successfully')
                this.portfolio = portfolio
                this.isFetching = false
                this.buildCharts()
            },
            error: (error): void => {
                this.log.error('Failed to fetch portfolio', error)
                this.isFetching = false
            },
        })

        this.log.debug('Fetching display currencies...')
        this.displayCurrenciesService.getAllCurrencies().subscribe({
            next: (response): void => {
                this.log.info(`Fetched ${response.currencies.length} currencies`)
                this.currencies = response.currencies.slice(0, 8)
                this.fetchPerformanceForCurrencies()
            },
            error: (error): void => {
                this.log.error('Failed to fetch display currencies', error)
                this.isFetchingCurrencies = false
            },
        })

        this.log.debug('Fetching all trade events...')
        this.allTradeEventsService.getAllTradeEvents().subscribe({
            next: (response): void => {
                this.log.info(`Fetched ${response.events.length} trade events`)
                this.recentTrades = response.events.slice(0, 5)
                this.isFetchingTrades = false
            },
            error: (error): void => {
                this.log.error('Failed to fetch trade events', error)
                this.isFetchingTrades = false
            },
        })
    }

    private buildCharts(): void {
        const assets: PortfolioAsset[] = this.portfolio.assets
        if (assets.length === 0) {
            return
        }

        const labels: string[] = assets.map((a: PortfolioAsset): string => a.currencyName)
        const values: number[] = assets.map((a: PortfolioAsset): number => a.totalValueInDollars)
        const colors: string[] = assets.map(
            (_: PortfolioAsset, i: number): string =>
                this.chartPalette[i % this.chartPalette.length],
        )

        this.allocationChartData = {
            labels,
            datasets: [
                {
                    data: values,
                    backgroundColor: colors,
                    borderWidth: 0,
                    hoverBorderWidth: 2,
                    hoverBorderColor: '#3eb98d',
                    hoverOffset: 8,
                },
            ],
        }

        this.assetBarChartData = {
            labels,
            datasets: [
                {
                    data: values,
                    backgroundColor: colors,
                    borderRadius: 6,
                    barThickness: 24,
                    borderSkipped: false,
                },
            ],
        }
    }

    protected navigateTo(route: string): void {
        void this.router.navigate([route])
    }

    protected getTotalWorth(): string {
        return this.currencyFormatter.formatCurrency(this.portfolio.totalWorth)
    }

    protected getDollarBalance(): string {
        return this.currencyFormatter.formatCurrency(this.portfolio.dollarBalance)
    }

    protected getShareBalance(): string {
        return this.currencyFormatter.formatCurrency(this.portfolio.shareBalance)
    }

    protected getLastUpdated(): string {
        return this.timeFormatter.formatTime(this.portfolio.lastUpdated)
    }

    protected getAssetCount(): string {
        return this.portfolio.assets.length.toString()
    }

    protected getCashAllocationPercent(): string {
        if (this.portfolio.totalWorth === 0) {
            return '0'
        }
        return ((this.portfolio.dollarBalance / this.portfolio.totalWorth) * 100).toFixed(1)
    }

    protected getCryptoAllocationPercent(): string {
        if (this.portfolio.totalWorth === 0) {
            return '0'
        }
        const cryptoValue: number = this.portfolio.totalWorth - this.portfolio.dollarBalance
        return ((cryptoValue / this.portfolio.totalWorth) * 100).toFixed(1)
    }

    protected hasAssets(): boolean {
        return this.portfolio.assets.length > 0
    }

    private fetchPerformanceForCurrencies(): void {
        let completed: number = 0
        const total: number = this.currencies.length
        if (total === 0) {
            this.isFetchingCurrencies = false
            return
        }
        for (const currency of this.currencies) {
            this.currencyDayPerformanceService
                .getCurrencyDayPerformance(currency.currencyCode)
                .subscribe({
                    next: (performance: PerformanceRating): void => {
                        this.currencyPerformanceMap.set(currency.currencyCode, performance)
                        completed++
                        if (completed === total) {
                            this.sortCurrenciesByPerformance()
                            this.isFetchingCurrencies = false
                        }
                    },
                    error: (): void => {
                        completed++
                        if (completed === total) {
                            this.sortCurrenciesByPerformance()
                            this.isFetchingCurrencies = false
                        }
                    },
                })
        }
    }

    private sortCurrenciesByPerformance(): void {
        this.currencies.sort((a: DisplayCurrency, b: DisplayCurrency): number => {
            const perfA: PerformanceRating | undefined = this.currencyPerformanceMap.get(
                a.currencyCode,
            )
            const perfB: PerformanceRating | undefined = this.currencyPerformanceMap.get(
                b.currencyCode,
            )
            const valA: number = this.parseChangePercent(perfA)
            const valB: number = this.parseChangePercent(perfB)
            return valB - valA
        })
    }

    private parseChangePercent(perf: PerformanceRating | undefined): number {
        if (!perf) {
            return 0
        }
        const cleaned: string = perf.changePercent.replace('%', '').replace('+', '')
        const parsed: number = parseFloat(cleaned)
        return isNaN(parsed) ? 0 : parsed
    }

    protected getPerformance(currencyCode: string): PerformanceRating {
        return (
            this.currencyPerformanceMap.get(currencyCode) ?? {
                rating: 'neutral',
                changePercent: '0%',
            }
        )
    }

    protected readonly TagType: typeof TagType = TagType
    protected readonly dashboardPageTitleStripe = dashboardPageTitleStripe
}
