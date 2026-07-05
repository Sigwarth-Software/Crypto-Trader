// trade.component.ts
import { Component, OnInit } from '@angular/core';

import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import {LoggerContext} from "@models/logging/LoggerContext";

@Component({
    selector: 'trade',
    templateUrl: './trade.component.html',
    styleUrls: ['./trade.component.scss'],
    standalone: false,
})
export class TradeComponent implements OnInit {
    constructor(private readonly logger: CryptoTraderLoggerService) {}

    ngOnInit(): void {
        this.logger.setContext(LoggerContext.Trade)
    }
}
