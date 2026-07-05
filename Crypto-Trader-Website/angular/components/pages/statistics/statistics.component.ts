// statistics.component.ts
import { Component, OnInit } from '@angular/core';

import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import {LoggerContext} from "@models/logging/LoggerContext";

@Component({
    selector: 'statistics',
    templateUrl: './statistics.component.html',
    styleUrls: ['./statistics.component.scss'],
    standalone: false,
})
export class StatisticsComponent implements OnInit {
    constructor(private readonly logger: CryptoTraderLoggerService) {}

    ngOnInit(): void {
        this.logger.setContext(LoggerContext.Statistics)
    }
}
