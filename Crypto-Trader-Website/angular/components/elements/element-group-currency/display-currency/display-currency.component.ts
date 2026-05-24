// display-currency.component.ts
import {
    AfterViewInit,
    Component,
    HostBinding,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChanges,
} from '@angular/core';
import { interval, Subject, Subscription, takeUntil } from 'rxjs';

import { ElementSize, TagType, WebSocketCapable } from '@theoliverlear/angular-suite';
import { defaultChartConfig } from '@assets/chartAssets';
import { defaultCurrencyIcon, ImageAsset } from '@assets/imageAssets';
import { CurrencyValueWsService } from '@ws/currency-value-ws.service';
import { CurrencyDayPerformanceService } from '@http/currency/currency-day-performance.service';
import { CurrencyHistoryService } from '@http/currency/currency-history.service';
import { CurrencyFormatterService } from '@ui/currency-formatter.service';
import { NumberTweenService } from '@ui/number-tween.service';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import { type ChartConfig, type SparkPoint } from '@models/chart/types';
import { DisplayCurrency, HistoryPoint, PerformanceRating } from '@models/currency/types';

/** Displays a currency's price and performance.
 *
 */
@Component({
    selector: 'display-currency',
    standalone: false,
    templateUrl: './display-currency.component.html',
    styleUrls: ['./display-currency.component.scss'],
    providers: [CurrencyValueWsService],
})
export class DisplayCurrencyComponent
    implements OnChanges, OnInit, AfterViewInit, OnDestroy, WebSocketCapable
{
    @Input() public currency: DisplayCurrency;
    protected imageAsset: ImageAsset = defaultCurrencyIcon;
    protected history: HistoryPoint[] = [];
    protected performance: PerformanceRating = {
        rating: 'neutral',
        changePercent: '0%',
    };
    protected currencyPrice: string = '';
    protected chartConfig: ChartConfig = { ...defaultChartConfig, dimensions: { ...defaultChartConfig.dimensions } };
    private currentNumericPrice: number = 0;
    private priceAnimationSub: Subscription | null = null;

    /** On upward ratings, point arrow upwards.
     * @returns {boolean} true if upward performance, false otherwise.
     */
    @HostBinding('class.up-performance')
    public get isUpPerformance(): boolean {
        return this.performance.rating === 'up';
    }

    /** On downward ratings, point arrow downwards.
     * @returns {boolean} true if downward performance, false otherwise.
     */
    @HostBinding('class.down-performance')
    public get isDownPerformance(): boolean {
        return this.performance.rating === 'down';
    }

    constructor(
        private readonly currencyFormatter: CurrencyFormatterService,
        private readonly historyService: CurrencyHistoryService,
        private readonly dayPerformance: CurrencyDayPerformanceService,
        private readonly currencyValueWebSocket: CurrencyValueWsService,
        private readonly numberTween: NumberTweenService,
        private readonly logger: CryptoTraderLoggerService,
    ) {}

    public webSocketSubscriptions: Record<string, Subscription> = {};
    /** Initialize web sockets for currency value updates.
     *
     */
    public initializeWebSockets(): void {
        this.logger.debug(`Initializing WebSockets for ${this.currency?.currencyCode}`, 'DisplayCurrency');
        this.currencyValueWebSocket.connect();
        this.webSocketSubscriptions['currency-value'] = this.currencyValueWebSocket
            .getMessages()
            .subscribe({
                next: (message: string): void => {
                    const numeric: number = Number(message);
                    if (!isFinite(numeric)) {
                        this.logger.warn(`Received non-numeric price update: ${message}`, 'DisplayCurrency');
                        return;
                    }
                    this.setCurrencyNumericPrice(numeric);
                },
                error: (error: unknown): void => {
                    this.logger.error(`WebSocket error for ${this.currency?.currencyCode}: ${error}`, undefined, 'DisplayCurrency');
                },
            });
    }

    /** Sets the chart properties based on historical data.
     *
     */
    private setChartProperties(): void {
        const prices: SparkPoint[] = this.history.map(
            (point: HistoryPoint): SparkPoint => ({
                date: point.date,
                value: point.value,
            }),
        );
        this.chartConfig = {
            ...defaultChartConfig,
            dimensions: { ...defaultChartConfig.dimensions, margin: { ...defaultChartConfig.dimensions.margin } },
            data: prices,
        };
    }

    /** Initialize WebSockets on component initialization.
     *
     */
    public ngOnInit(): void {
        this.logger.info(`DisplayCurrencyComponent initialized for ${this.currency?.currencyCode}`, 'DisplayCurrency');
        this.initializeWebSockets();
    }

    private readonly destroy$: Subject<void> = new Subject<void>();

    /** Continuously update the currency price every 5 seconds via WebSocket.
     *
     */
    private continuouslyUpdatePrice(): void {
        this.currencyValueWebSocket.sendMessage(this.currency.currencyCode);
        interval(5000)
            .pipe(takeUntil(this.destroy$))
            .subscribe((): void => this.updatePrice());
    }

    /** Update price via WebSocket.
     *
     */
    private updatePrice(): void {
        this.currencyValueWebSocket.sendMessage(this.currency.currencyCode);
    }

    /** Update the currency price and numeric value.
     *
     * @param price
     */
    private setCurrencyPrice(price: string): void {
        if (this.currencyPrice === price) {
            return;
        }
        this.currencyPrice = price;
        const numeric: number = this.parseNumeric(price);
        if (isFinite(numeric)) {
            this.currentNumericPrice = numeric;
        }
    }

    /** Animate the currency price to a new value.
     *
     * @param nextPrice
     * @param durationMs
     */
    private setCurrencyNumericPrice(nextPrice: number, durationMs: number = 500): void {
        const from: number = this.getCurrentDisplayedNumeric();
        if (!isFinite(from) || from === 0) {
            this.currencyPrice = this.currencyFormatter.formatCurrency(nextPrice);
            this.currentNumericPrice = nextPrice;
            return;
        }
        if (nextPrice === from) {
            return;
        }

        if (this.priceAnimationSub) {
            this.priceAnimationSub.unsubscribe();
            this.priceAnimationSub = null;
        }
        this.currentNumericPrice = nextPrice;
        this.priceAnimationSub = this.numberTween
            .animate(from, nextPrice, durationMs)
            .subscribe((value: number): void => {
                this.currencyPrice = this.currencyFormatter.formatCurrency(value);
            });
    }

    private getCurrentDisplayedNumeric(): number {
        if (this.currencyPrice) {
            const parsed: number = this.parseNumeric(this.currencyPrice);
            if (isFinite(parsed)) return parsed;
        }

        if (isFinite(this.currentNumericPrice) && this.currentNumericPrice !== 0) {
            return this.currentNumericPrice;
        }
        return this.currency ? Number(this.currency.value) : 0;
    }

    private parseNumeric(formatted: string): number {
        const cleanedValue: string = formatted.replace(/[^0-9.+-]/g, '');
        return Number(cleanedValue);
    }

    /** Get the currency price as a formatted string.
     * @returns The formatted currency price.
     */
    protected getCurrencyPrice(): string {
        if (this.currency) {
            return this.currencyFormatter.formatCurrency(this.currency.value);
        } else {
            return this.currencyFormatter.formatCurrency(0);
        }
    }

    /** Updates image and price when currency changes.
     *
     * @param changes
     */
    public ngOnChanges(changes: SimpleChanges): void {
        if ('currency' in changes) {
            void this.resolveImageAsset();
            this.fetchHistory();
            const initValue: string = this.getCurrencyPrice();
            this.currencyPrice = initValue;
            const initNumericValue: number = this.parseNumeric(initValue);
            if (isFinite(initNumericValue)) {
                this.currentNumericPrice = initNumericValue;
            }
        }
        if ('history' in changes) {
            this.setChartProperties();
        }
        if ('performance' in changes) {
            this.updatePerformance();
        }
    }

    /** Initialize visual components after view initialization.
     *
     */
    public ngAfterViewInit(): void {
        void this.resolveImageAsset();
        this.updatePerformance();
        this.continuouslyUpdatePrice();
        // this.setChartProperties();
    }

    /** Clean up subscriptions and WebSocket connections on component destruction.
     *
     */
    public ngOnDestroy(): void {
        this.logger.debug(`Destroying DisplayCurrencyComponent for ${this.currency?.currencyCode}`, 'DisplayCurrency');
        this.destroy$.next();
        this.destroy$.complete();

        Object.values(this.webSocketSubscriptions).forEach((sub: Subscription): void =>
            sub?.unsubscribe(),
        );
        this.webSocketSubscriptions = {};
        if (this.priceAnimationSub) {
            this.priceAnimationSub.unsubscribe();
            this.priceAnimationSub = null;
        }
        try {
            this.currencyValueWebSocket.disconnect();
        } catch (error) {
            console.error('Failed to disconnect from WebSocket', error);
        }
    }

    /** Update the currency's performance rating.
     *
     */
    protected updatePerformance(): void {
        this.dayPerformance
            .getCurrencyDayPerformance(this.currency.currencyCode)
            .subscribe((performance: PerformanceRating): void => {
                this.logger.debug(`Performance updated for ${this.currency.currencyCode}: ${performance.changePercent}`, 'DisplayCurrency');
                this.performance = performance;
            });
    }

    /** Fetch the currency's historical price data if the currency is valid.
     *
     */
    protected fetchHistory(): void {
        if (!this.isCurrencyInvalid()) {
            void this.listenForCurrencyHistory();
        }
    }

    private listenForCurrencyHistory(): void {
        this.logger.debug(`Fetching history for ${this.currency.currencyCode}`, 'DisplayCurrency');
        this.historyService
            .getHistory(this.currency.currencyCode, 24, 60)
            .subscribe((points: HistoryPoint[]): void => {
                this.logger.debug(`History received for ${this.currency.currencyCode}: ${points.length} points`, 'DisplayCurrency');
                this.history = points;
                this.setChartProperties();
            });
    }

    private isCurrencyInvalid(): boolean {
        return !this.currency || !this.currency.currencyCode;
    }

    // TODO: Use service method to get image.
    /** Get the currency's image asset.
     *
     */
    protected async resolveImageAsset(): Promise<void> {
        if (!this.currency) {
            return;
        }
        const imageAsset: ImageAsset = {
            src: this.currency.logoUrl,
            alt: this.currency.currencyName + ' logo',
        };
        const imageLoads: boolean = await this.imageLoads(imageAsset.src);
        if (imageLoads) {
            this.imageAsset = imageAsset;
        } else {
            this.imageAsset = defaultCurrencyIcon;
        }
    }

    /**
     *
     * @param src
     */
    protected async imageLoads(src: string): Promise<boolean> {
        return new Promise<boolean>((resolve): void => {
            const image = new Image();
            image.onload = (): void => {
                resolve(true);
            };
            image.onerror = (): void => {
                resolve(false);
            };
            image.src = src;
        });
    }

    protected readonly TagType: typeof TagType = TagType;
    protected readonly ElementSize: typeof ElementSize = ElementSize;
}
