// trade-row.component.ts
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core'

import { CurrencyFormatterService } from '@ui/currency-formatter.service'
import { TimeFormatterService } from '@ui/time-formatter.service'
import { TradeEvent } from '@models/trader/types'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { LoggerContext } from '@models/logging/LoggerContext'
import { SharesFormatterService } from '@ui/shares-formatter.service'

/**
 * A component that displays a single trade event in a row format.
 */
@Component({
    selector: 'trade-row',
    standalone: false,
    templateUrl: './trade-row.component.html',
    styleUrls: ['./trade-row.component.scss'],
})
export class TradeRowComponent implements OnInit {
    // TODO: Remove non-null assertion.
    @Input() public trade!: TradeEvent
    @Output() public rowClick: EventEmitter<void> = new EventEmitter<void>()

    constructor(
        private readonly currencyFormatter: CurrencyFormatterService,
        private readonly timeFormatter: TimeFormatterService,
        private readonly log: CryptoTraderLoggerService,
        private readonly sharesFormatter: SharesFormatterService,
    ) {}

    /**
     * On init, set the logger context and log the trade event.
     */
    public ngOnInit(): void {
        this.log.setContext(LoggerContext.Dashboard)
        this.log.debug(`TradeRowComponent initialized for trade: ${this.trade.id}`)
    }

    protected formatValue(): string {
        const prefix: string = this.trade.valueChange >= 0 ? '+' : ''
        return `${prefix}${this.currencyFormatter.formatCurrency(this.trade.valueChange, true)}`
    }

    protected formatTime(): string {
        return this.timeFormatter.formatTime(this.trade.tradeTime)
    }

    protected getSharesChange(): string {
        if (this.trade.sharesChange > 0) {
            const formattedShares: string = this.sharesFormatter.formatShares(
                this.trade.sharesChange,
                this.trade.currency,
            )
            return `+${formattedShares}`
        }

        return '+0.00'
    }

    protected onClick(): void {
        this.log.info(`Trade row clicked: ${this.trade.id}`)
        this.rowClick.emit()
    }
}
