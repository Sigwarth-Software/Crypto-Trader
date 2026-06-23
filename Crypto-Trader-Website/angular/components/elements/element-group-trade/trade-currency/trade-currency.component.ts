// trade-currency.component.ts
import {
    AfterViewInit,
    ChangeDetectionStrategy,
    Component,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    SimpleChange,
    SimpleChanges,
} from '@angular/core';
import { interval, Subject, Subscription, takeUntil } from 'rxjs';

import { TagType, WebSocketCapable } from '@theoliverlear/angular-suite';
import { defaultCurrencyIcon, ImageAsset } from '@assets/image.assets';
import { CurrencyValueWsService } from '@ws/currency-value-ws.service';
import { CurrencyFormatterService } from '@ui/currency-formatter.service';
import { CurrencyImageService } from '@ui/currency-image.service';
import { NumberTweenService } from '@ui/number-tween.service';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import { Currency, DisplayCurrency } from '@models/currency/types';

@Component({
    selector: 'trade-currency',
    templateUrl: './trade-currency.component.html',
    styleUrls: ['./trade-currency.component.scss'],
    standalone: false,
})
class TradeCurrencyComponent
    implements OnInit, OnChanges, WebSocketCapable, OnDestroy
{
    @Input() selectedCurrency: Currency;
    currencyValue: number = 0;
    protected currencyPrice: string = '$0.00';
    imageAsset: ImageAsset = defaultCurrencyIcon;
    private previousNumericPrice: number = 0;
    private currentNumericPrice: number = 0;
    private priceAnimationSub: Subscription | null = null;
    private destroy$: Subject<void> = new Subject<void>();
    constructor(
        private currencyImageService: CurrencyImageService,
        private currencyValueWebSocket: CurrencyValueWsService,
        private currencyFormatter: CurrencyFormatterService,
        private numberTween: NumberTweenService,
        private readonly logger: CryptoTraderLoggerService,
    ) {}

    webSocketSubscriptions: Record<string, Subscription> = {};
    initializeWebSockets(): void {
        this.logger.debug(`Initializing WebSockets for ${this.selectedCurrency?.currencyCode}`, 'TradeCurrency');
        this.currencyValueWebSocket.connect();
        this.webSocketSubscriptions['currency-value'] =
            this.currencyValueWebSocket.getMessages().subscribe({
                next: (message) => {
                    if (!message) {
                        return;
                    }
                    const numeric: number = Number(message);
                    if (!isFinite(numeric)) {
                        this.logger.warn(`Received non-numeric price update: ${message}`, 'TradeCurrency');
                        return;
                    }
                    this.setCurrencyNumericPrice(numeric);
                },
                error: (error) => {
                    this.logger.error(`WebSocket error for ${this.selectedCurrency?.currencyCode}`, error, 'TradeCurrency');
                },
            });
    }

    continuouslyUpdatePrice(): void {
        this.updatePrice();
        interval(5000)
            .pipe(takeUntil(this.destroy$))
            .subscribe(() => this.updatePrice());
    }

    updatePrice(): void {
        this.currencyValueWebSocket.sendMessage(
            this.selectedCurrency.currencyCode,
        );
    }

    private parseNumeric(formatted: string): number {
        const cleanedValue: string = formatted.replace(/[^0-9.+-]/g, '');
        return Number(cleanedValue);
    }

    private getCurrentDisplayedNumeric(): number {
        if (this.currencyPrice) {
            const parsed: number = this.parseNumeric(this.currencyPrice);
            if (isFinite(parsed)) return parsed;
        }

        if (
            isFinite(this.currentNumericPrice) &&
            this.currentNumericPrice !== 0
        ) {
            return this.currentNumericPrice;
        }
        return this.selectedCurrency ? Number(this.selectedCurrency.value) : 0;
    }
    setCurrencyNumericPrice(to: number, durationMs: number = 500): void {
        const from: number = this.getCurrentDisplayedNumeric();
        if (!isFinite(from) || from === 0) {
            this.currencyPrice = this.currencyFormatter.formatCurrency(to);
            this.previousNumericPrice = to;
            this.currentNumericPrice = to;
            return;
        }
        if (to === from) {
            return;
        }

        if (this.priceAnimationSub) {
            this.priceAnimationSub.unsubscribe();
            this.priceAnimationSub = null;
        }
        this.previousNumericPrice = from;
        this.currentNumericPrice = to;
        this.priceAnimationSub = this.numberTween
            .animate(from, to, durationMs)
            .subscribe((value) => {
                this.currencyPrice =
                    this.currencyFormatter.formatCurrency(value);
            });
    }

    ngOnChanges(changes: SimpleChanges) {
        const currentState: SimpleChange = changes['selectedCurrency'];
        if (!currentState || !currentState.currentValue) return;

        const prevCode = currentState.previousValue?.currencyCode;
        const nextCode = currentState.currentValue.currencyCode;
        if (prevCode === nextCode) return;

        Promise.resolve().then(() => this.setImageAsset());
    }

    ngOnInit() {
        this.logger.info(`TradeCurrencyComponent initialized for ${this.selectedCurrency?.currencyCode}`, 'TradeCurrency');
        this.currencyPrice = this.currencyFormatter.formatCurrency(
            this.selectedCurrency.value,
        );
        this.initializeWebSockets();
        this.continuouslyUpdatePrice();
        this.setImageAsset();
    }

    async setImageAsset(): Promise<void> {
        if (!this.selectedCurrency) {
            return;
        }
        const displayCurrency: DisplayCurrency = {
            currencyCode: this.selectedCurrency.currencyCode,
            currencyName: this.selectedCurrency.currencyName,
            value: this.currencyValue,
            logoUrl: this.getImageUrl(this.selectedCurrency.currencyCode),
        };
        const imageAsset: ImageAsset =
            await this.currencyImageService.resolveImageAsset(displayCurrency);
        this.imageAsset = imageAsset;
    }

    private getImageUrl(currencyCode: string): string {
        return `/assets/cryptofont/${currencyCode.toLowerCase()}.svg`;
    }
    ngOnDestroy(): void {
        this.logger.debug(`Destroying TradeCurrencyComponent for ${this.selectedCurrency?.currencyCode}`, 'TradeCurrency');
        this.destroy$.next();
        this.destroy$.complete();

        Object.values(this.webSocketSubscriptions).forEach((sub) =>
            sub?.unsubscribe(),
        );
        this.webSocketSubscriptions = {};
        if (this.priceAnimationSub) {
            this.priceAnimationSub.unsubscribe();
            this.priceAnimationSub = null;
        }
        try {
            this.currencyValueWebSocket.disconnect();
        } catch {
            console.error('Failed to disconnect from WebSocket');
        }
    }

    protected readonly TagType = TagType;
}

export default TradeCurrencyComponent;
