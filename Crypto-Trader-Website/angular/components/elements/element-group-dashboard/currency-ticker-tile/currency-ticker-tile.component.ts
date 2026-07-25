// currency-ticker-tile.component.ts
import {
    AfterViewInit,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
} from '@angular/core'

import { CurrencyFormatterService } from '@ui/currency-formatter.service'
import { DisplayCurrency, PerformanceRating } from '@models/currency/types'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { CurrencyImageService } from '@ui/currency-image.service'
import { defaultCurrencyIcon, ImageAsset } from '@assets/image.assets'
import { LoggerContext } from '@models/logging/LoggerContext'
import { ImageColorVariation } from '@models/image/ImageColorVariation'

/**
 * A component that displays a currency ticker.
 */
@Component({
    selector: 'currency-ticker-tile',
    standalone: false,
    templateUrl: './currency-ticker-tile.component.html',
    styleUrls: ['./currency-ticker-tile.component.scss'],
})
export class CurrencyTickerTileComponent implements OnInit, AfterViewInit, OnChanges {
    // TODO: Remove non-null assertion.
    @Input() public currency!: DisplayCurrency
    // Extract to a default asset.
    @Input() public performance: PerformanceRating = { rating: 'neutral', changePercent: '0%' }
    @Output() public tileClick: EventEmitter<void> = new EventEmitter<void>()
    protected imageAsset: ImageAsset = defaultCurrencyIcon

    constructor(
        private readonly currencyFormatter: CurrencyFormatterService,
        private readonly logger: CryptoTraderLoggerService,
        private readonly currencyImageService: CurrencyImageService,
    ) {}

    /**
     * On init, set the logger context.
     */
    public ngOnInit(): void {
        this.logger.setContext(LoggerContext.Currencies)
    }

    /**
     * After view is init, resolve the image asset.
     */
    public ngAfterViewInit(): void {
        void this.resolveImageAsset()
    }

    /**
     * On changes, resolve the image asset if the currency changes.
     *
     * @param changes
     */
    public ngOnChanges(changes: SimpleChanges): void {
        if ('currency' in changes) {
            void this.resolveImageAsset()
        }
    }

    protected formatPrice(): string {
        return this.currencyFormatter.formatCurrency(this.currency.value)
    }

    protected async resolveImageAsset(): Promise<void> {
        if (!this.currency) {
            this.imageAsset = defaultCurrencyIcon
            return
        }
        this.imageAsset = await this.currencyImageService.resolveImageAsset(
            this.currency.currencyCode,
        )
    }

    protected onClick(): void {
        this.logger.info(`Currency ticker tile clicked: ${this.currency.currencyCode}`)
        this.tileClick.emit()
    }

    protected readonly ImageColorVariation: typeof ImageColorVariation = ImageColorVariation
}
