// trade-checkout-details.component.ts
import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChange,
    type SimpleChanges,
} from '@angular/core';
import { interval, Subject, Subscription, takeUntil } from 'rxjs';

import { ElementSize, InputType, TagType, WebSocketCapable } from '@theoliverlear/angular-suite';
import { CurrencyValueWsService } from '@ws/currency-value-ws.service';
import { TradeCheckoutService } from '@http/trade/trade-checkout.service';
import { CurrencyFormatterService } from '@ui/currency-formatter.service';
import { NumberTweenService } from '@ui/number-tween.service';
import { SharesFormatterService } from '@ui/shares-formatter.service';
import { DisplayCurrency } from '@models/currency/types';
import { TradeCheckout } from '@models/trade/TradeCheckout';
import { TradeCheckoutRequest } from '@models/trade/types';
import { OperationSuccessResponse } from '@models/types';

import { BuyType } from '../buy-type/models/BuyType';

/** A component for displaying trade checkout details.
 *
 */
@Component({
    selector: 'trade-checkout-details',
    templateUrl: './trade-checkout-details.component.html',
    styleUrls: ['./trade-checkout-details.component.scss'],
    standalone: false,
})
export class TradeCheckoutDetailsComponent
    implements WebSocketCapable, OnInit, OnDestroy, OnChanges
{
    @Input() public displayCurrency: DisplayCurrency;
    protected numDollars: number = 0;
    protected numShares: number = 0;
    protected numFeeDollars: number = 0;

    protected youPayText: string = '';
    protected youGetText: string = '';

    private readonly tradeCheckout: TradeCheckout = new TradeCheckout();
    @Output() public readonly tradeCheckoutChange: EventEmitter<TradeCheckout> =
        new EventEmitter<TradeCheckout>();
    protected buyType: BuyType = BuyType.DOLLARS;
    private priceAnimationSub: Subscription | null = null;
    private readonly destroy$: Subject<void> = new Subject<void>();

    constructor(
        private readonly currencyFormatter: CurrencyFormatterService,
        private readonly sharesFormatter: SharesFormatterService,
        private readonly currencyValueWebSocket: CurrencyValueWsService,
        private readonly numberTween: NumberTweenService,
        private readonly tradeCheckoutService: TradeCheckoutService,
    ) {}

    /** Attempts to checkout the trade.
     *
     */
    protected attemptCheckout(): void {
        try {
            const tradeRequest: TradeCheckoutRequest = this.tradeCheckout.getRequest();
            this.tradeCheckoutService.checkout(tradeRequest).subscribe({
                next: (response: OperationSuccessResponse): void => {
                    console.log('Trade checkout successful:', response);
                },
            });
        } catch (error) {
            console.error('Failed to create trade checkout request:', error);
            return;
        }
    }

    /** On changes, updates display currency.
     *
     * @param changes
     */
    public ngOnChanges(changes: SimpleChanges): void {
        const displayCurrencyChange: SimpleChange = changes['displayCurrency'];
        if (!displayCurrencyChange || !displayCurrencyChange.currentValue) return;
        this.displayCurrency = displayCurrencyChange.currentValue as DisplayCurrency;
        this.tradeCheckout.currencyCode = this.displayCurrency.currencyCode;
    }

    /** On destruction, clean up subscriptions and WebSocket connections.
     *
     */
    public ngOnDestroy(): void {
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
        } catch {
            console.error('Failed to disconnect from WebSocket');
        }
    }

    /** On init, initialize WebSockets and update trade texts.
     *
     */
    public ngOnInit(): void {
        // this.youPayText = this.getYouPayText();
        // this.youGetText = this.getYouGetText();
        this.updateTradeTexts();
        this.initializeWebSockets();
        this.continuouslyUpdatePrice();
    }

    public webSocketSubscriptions: Record<string, Subscription> = {};
    /** Initializes the WebSockets for real-time currency price updates.
     *
     */
    public initializeWebSockets(): void {
        this.currencyValueWebSocket.connect();
        this.webSocketSubscriptions['currency-value'] = this.currencyValueWebSocket
            .getMessages()
            .subscribe({
                next: (message: string): void => {
                    // if (!message) {
                    //     return;
                    // }
                    const numericPrice: number = Number(message);
                    // if (!isFinite(numericPrice)) {
                    //     return;
                    // }
                    this.updatePriceDisplay(numericPrice);
                },
            });
    }

    /** Tweens the display currency value to the target price.
     *
     * @param nextPrice
     * @param durationMs
     */
    protected updatePriceDisplay(nextPrice: number, durationMs: number = 500): void {
        const previousPrice: number = this.displayCurrency.value;
        if (!isFinite(previousPrice) || previousPrice === 0) {
            this.displayCurrency.value = nextPrice;
            this.updateTradeTexts();
            return;
        }
        if (nextPrice === previousPrice) {
            this.updateTradeTexts();
            return;
        }

        if (this.priceAnimationSub) {
            this.priceAnimationSub.unsubscribe();
            this.priceAnimationSub = null;
        }
        this.priceAnimationSub = this.numberTween
            .animate(previousPrice, nextPrice, durationMs)
            .subscribe((value: number): void => {
                this.displayCurrency.value = value;
                this.updateTradeTexts();
            });
    }

    /** Updates the display currency's price.
     *
     * @param currencyPrice
     */
    protected setCurrencyPrice(currencyPrice: number): void {
        this.displayCurrency.value = currencyPrice;
    }

    /**
     * Continuously updates the display currency's price.
     */
    private continuouslyUpdatePrice(): void {
        this.updatePrice();
        interval(5000)
            .pipe(takeUntil(this.destroy$))
            .subscribe((): void => this.updatePrice());
    }

    /**
     * Calls WebSocket to update the display currency's price.
     */
    private updatePrice(): void {
        this.currencyValueWebSocket.sendMessage(this.displayCurrency.currencyCode);
    }

    /** Checks if the trade has a vendor fee.
     * @returns true if the trade has a vendor fee, false otherwise.
     */
    protected hasVendorFee(): boolean {
        return this.numFeeDollars > 0;
    }

    /** Sets trade input with sanitization and validation.
     *
     * @param input
     */
    protected setFromInput(input: string): void {
        const inputAsNumber: number = Number(input);
        if (isNaN(inputAsNumber)) {
            return;
        }
        if (this.buyType === BuyType.DOLLARS) {
            this.numDollars = inputAsNumber;
            this.tradeCheckout.numDollars = inputAsNumber;
        } else {
            this.numShares = inputAsNumber;
            this.tradeCheckout.numShares = inputAsNumber;
        }
        this.updateTradeTexts();
    }

    /** Changes buying and selling state of trade checkout.
     *
     * @param buyType
     */
    protected setBuyType(buyType: BuyType): void {
        if (this.buyType === BuyType.DOLLARS) {
            this.numShares = this.numDollars;
            this.tradeCheckout.numShares = this.numDollars;
            this.numDollars = 0;
            this.tradeCheckout.numShares = 0;
        } else {
            this.numDollars = this.numShares;
            this.tradeCheckout.numDollars = this.numShares;
            this.numShares = 0;
            this.tradeCheckout.numShares = 0;
        }
        this.buyType = buyType;
        this.updateTradeTexts();
    }

    private updateTradeTexts(): void {
        this.youPayText = this.getYouPayText();
        this.youGetText = this.getYouGetText();
    }

    /** Gets vendor fee for trade checkout.
     * @returns vendor fee as string.
     */
    protected getVendorFee(): string {
        // TODO: Implement vendor fee fetching from API.
        return '';
    }

    /** Gets the "You Pay" text based on the current buy type and input values.
     * @returns "You Pay" text as string.
     */
    protected getYouPayText(): string {
        if (this.buyType === BuyType.SHARES) {
            const numDollars: number = this.getSharesValue();
            return this.currencyFormatter.formatCurrency(numDollars);
        } else {
            return this.currencyFormatter.formatCurrency(this.numDollars);
        }
    }

    private getSharesValue(): number {
        return this.displayCurrency.value * this.numShares;
    }

    /** Gets the "You Get" text based on the current buy type and input values.
     * @returns "You Get" text as string.
     */
    protected getYouGetText(): string {
        if (this.buyType === BuyType.SHARES) {
            return this.sharesFormatter.formatShares(
                this.numShares,
                this.displayCurrency.currencyCode,
            );
        } else {
            const numShares: number = this.numDollars / this.displayCurrency.value;
            return this.sharesFormatter.formatShares(numShares, this.displayCurrency.currencyCode);
        }
    }

    /** Determines precision for number of shares based on value.
     *
     * @param numShares
     * @returns number of decimal places to display.
     */
    protected getNumDecimals(numShares: number): number {
        return numShares < 1 ? 10 : 2;
    }

    /** Gets placeholder text based on buy type.
     * @returns placeholder text as string.
     */
    protected getPlaceholderText(): string {
        if (this.buyType === BuyType.DOLLARS) {
            return '# of Dollars';
        } else {
            return '# of Shares';
        }
    }

    protected readonly TagType: typeof TagType = TagType;
    protected readonly InputType: typeof InputType = InputType;
    protected readonly ElementSize: typeof ElementSize = ElementSize;
}
