// portfolio-overview.component.ts
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'
import { ImageAsset } from '@assets/image.assets'
import { CurrencyFormatterService } from '@ui/currency-formatter.service'
import { CurrencyImageService } from '@ui/currency-image.service'
import { SharesFormatterService } from '@ui/shares-formatter.service'
import { TimeFormatterService } from '@ui/time-formatter.service'
import { Portfolio, PortfolioAsset } from '@models/portfolio/types'
import {
    BaseLoggingElementComponent
} from '@components/elements/element-group-system/base-logging-element/base-logging-element.component'
import { LoggerContext } from '@models/logging/LoggerContext'

// TODO: Move to types or asset file.
type ImageWithText = [string, ImageAsset]

/**
 * A component with a summary of a portfolio.
 */
@Component({
    selector: 'portfolio-overview',
    templateUrl: './portfolio-overview.component.html',
    styleUrls: ['./portfolio-overview.component.scss'],
    standalone: false,
})
export class PortfolioOverviewComponent extends BaseLoggingElementComponent implements OnInit, OnChanges {
    @Input() public portfolio: Portfolio
    protected currencyImagesValues: ImageWithText[] = []

    constructor(
        private readonly currencyFormatter: CurrencyFormatterService,
        private readonly timeFormatter: TimeFormatterService,
        private readonly currencyImageService: CurrencyImageService,
        private readonly sharesFormatter: SharesFormatterService,
    ) {
        super()
        this.loggerContext = LoggerContext.Portfolio
    }

    /**
     * On changes, load new currency images.
     * @param changes
     */
    public ngOnChanges(changes: SimpleChanges): void {
        if ('portfolio' in changes) {
            void this.loadCurrencyImages()
        }
    }

    /**
     * On component initialization, load currency images.
     *
     */
    public ngOnInit(): void {
        void this.loadCurrencyImages()
    }

    /**
     * Checks if the portfolio has any assets.
     * @returns True if the portfolio has assets, false otherwise.
     */
    protected hasCurrencies(): boolean {
        return this.portfolio.assets.length > 0
    }

    /**
     * Loads currency images for each asset in the portfolio.
     */
    private async loadCurrencyImages(): Promise<void> {
        this.currencyImagesValues = await Promise.all(
            [...this.portfolio.assets]
                .sort((a, b): number => b.totalValueInDollars - a.totalValueInDollars)
                .map(async (asset: PortfolioAsset): Promise<ImageWithText> => {
                    const currencyImage: ImageAsset =
                        await this.currencyImageService.resolveImageAsset(asset.currencyCode)
                    const assetValue: string = this.currencyFormatter.formatCurrency(
                        asset.totalValueInDollars,
                    )
                    return [assetValue, currencyImage] as ImageWithText
                }),
        )
    }

    /**
     * A formatted string of the portfolio's dollar balance.
     * @returns A formatted string representing the dollar balance.
     */
    protected getDollarBalance(): string {
        return this.currencyFormatter.formatCurrency(this.portfolio.dollarBalance)
    }

    /**
     * A formatted string of the portfolio's share balance.
     * @returns A formatted string representing the share balance.
     */
    protected getSharesBalance(): string {
        return this.currencyFormatter.formatCurrency(this.portfolio.shareBalance)
    }

    /**
     * A formatted string of the total worth of the portfolio.
     * @returns A formatted string representing the total worth.
     */
    protected getTotalWorth(): string {
        return this.currencyFormatter.formatCurrency(this.portfolio.totalWorth)
    }

    /**
     * Provides a formatted last traded time for the portfolio.
     * @returns A readable time string.
     */
    protected getLastTraded(): string {
        return this.timeFormatter.formatTime(this.portfolio.lastTraded)
    }

    protected readonly TagType: typeof TagType = TagType
}
