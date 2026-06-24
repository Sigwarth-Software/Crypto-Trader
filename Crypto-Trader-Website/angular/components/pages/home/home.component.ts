import { Component, OnInit } from '@angular/core';

import { ElementSize } from '@theoliverlear/angular-suite';
import { getStartedElementLink } from '@assets/element-link.assets';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import {LoggerContext} from "@models/logging/LoggerContext";

@Component({
    selector: 'home',
    standalone: false,
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
    constructor(private readonly log: CryptoTraderLoggerService) {}

    public ngOnInit(): void {
        this.log.setContext(LoggerContext.Home);
    }

    protected readonly ElementSize = ElementSize;
    protected readonly getStartedElementLink = getStartedElementLink;
    // TODO: Move to assets.
    protected readonly flipWords: string[] = [
        'a security gateway',
        'a machine learning engine',
        'a real-time data backbone',
        'a trade execution core',
        'an AI-powered chatbot',
        'a health monitoring system',
        'an observability platform',
        'a desktop admin portal',
    ];
}
