// account.component.ts
import { Component, OnInit } from '@angular/core';

import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import {LoggerContext} from "@models/logging/LoggerContext";

@Component({
    selector: 'account',
    standalone: false,
    templateUrl: './account.component.html',
    styleUrls: ['./account.component.scss'],
})
export class AccountComponent implements OnInit {
    constructor(private readonly log: CryptoTraderLoggerService) {}

    ngOnInit(): void {
        this.log.setContext(LoggerContext.Account)
    }
}
