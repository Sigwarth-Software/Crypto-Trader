import { Component, OnInit } from '@angular/core';

import { TagType } from '@theoliverlear/angular-suite';
import { defaultPortfolio } from '@assets/portfolio.assets';
import { PortfolioService } from '@http/portfolio/portfolio.service';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import { Portfolio } from '@models/portfolio/types';

import { PortfolioSectionArrowType } from '../../elements/element-group-portfolio/portfolio-section-arrow/models/PortfolioSectionArrowType';
import {LoggerContext} from "@models/logging/LoggerContext";

@Component({
    selector: 'portfolio',
    standalone: false,
    templateUrl: './portfolio.component.html',
    styleUrls: ['./portfolio.component.scss'],
})
export class PortfolioComponent implements OnInit {
    portfolio: Portfolio = defaultPortfolio;
    constructor(
        private portfolioService: PortfolioService,
        private readonly log: CryptoTraderLoggerService,
    ) {}

    ngOnInit(): void {
        this.log.setContext(LoggerContext.Portfolio);
        this.log.info('Portfolio component initialized');

        this.log.debug('Fetching portfolio...');
        this.portfolioService.getPortfolio().subscribe({
            next: (data: Portfolio): void => {
                this.log.info('Portfolio fetched successfully');
                this.portfolio = data;
            },
            error: (error): void => {
                this.log.error('Failed to fetch portfolio', error);
            },
        });
    }

    hasCurrencies(): boolean {
        return this.portfolio.assets.length > 0;
    }

    protected readonly TagType = TagType;
    protected readonly PortfolioSectionArrowType = PortfolioSectionArrowType;
}
