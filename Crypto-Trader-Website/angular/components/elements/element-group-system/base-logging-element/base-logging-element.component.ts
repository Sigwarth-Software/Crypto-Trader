import { Directive, inject, Input, OnInit } from '@angular/core'
import {
    CryptoTraderLoggerService
} from '@services/logging/crypto-trader-logger.service'
import { LoggerContext } from '@models/logging/LoggerContext'

/**
 * A base component that accepts and performs logging operations.
 */
@Directive()
export abstract class BaseLoggingElementComponent implements OnInit {
    @Input() public loggerContext: LoggerContext = LoggerContext.System
    protected readonly log: CryptoTraderLoggerService = inject(CryptoTraderLoggerService)

    /**
     * On init, set the logger context.
     */
    public ngOnInit(): void {
        this.log.setContext(this.loggerContext)
    }
}
