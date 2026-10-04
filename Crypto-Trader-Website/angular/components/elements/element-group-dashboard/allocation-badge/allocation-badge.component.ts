// allocation-badge.component.ts
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core'

import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { LoggerContext } from '@models/logging/LoggerContext'

/**
 * A badge component that displays portfolio allocations.
 */
@Component({
    selector: 'allocation-badge',
    standalone: false,
    templateUrl: './allocation-badge.component.html',
    styleUrls: ['./allocation-badge.component.scss'],
})
export class AllocationBadgeComponent implements OnInit {
    @Input() public label: string = ''
    @Input() public value: string = ''
    // TODO: Extract type.
    @Input() public variant: 'cash' | 'crypto' | 'assets' = 'cash'
    @Output() public badgeClick: EventEmitter<void> = new EventEmitter<void>()

    constructor(private readonly logger: CryptoTraderLoggerService) {}

    /**
     * On init, set the logger context.
     */
    public ngOnInit(): void {
        this.logger.setContext(LoggerContext.System)
    }

    protected onClick(): void {
        this.logger.info(`Allocation badge clicked: ${this.label}`)
        this.badgeClick.emit()
    }
}
