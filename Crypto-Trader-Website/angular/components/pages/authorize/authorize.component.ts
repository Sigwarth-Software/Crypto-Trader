import { Component, OnInit } from '@angular/core';

import { AuthPopup, WebSocketCapable } from '@theoliverlear/angular-suite';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import {LoggerContext} from "@models/logging/LoggerContext";

@Component({
    selector: 'authorize',
    standalone: false,
    templateUrl: './authorize.component.html',
    styleUrls: ['./authorize.component.scss'],
})
export class AuthorizeComponent implements OnInit {
    authPopup: AuthPopup = AuthPopup.NONE;
    constructor(private readonly logger: CryptoTraderLoggerService) {}

    ngOnInit(): void {
        this.logger.setContext(LoggerContext.Auth)
    }

    setAuthPopup(authPopup: AuthPopup): void {
        this.logger.debug(`Setting auth popup to ${authPopup}`);
        this.authPopup = authPopup;
    }

    protected readonly AuthPopup = AuthPopup;
}
