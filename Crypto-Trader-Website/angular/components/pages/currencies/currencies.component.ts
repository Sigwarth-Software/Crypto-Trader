// currencies.component.ts
import { Component, HostListener, OnInit } from '@angular/core';

import { DisplayCurrenciesService } from '@http/currency/display-currencies.service';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import { DisplayCurrencyList } from '@models/currency/types';

import { listStagger } from '../../animations/animations';
import {LoggerContext} from "@models/logging/LoggerContext";

@Component({
    selector: 'currencies',
    standalone: false,
    templateUrl: './currencies.component.html',
    styleUrls: ['./currencies.component.scss'],
    animations: [listStagger],
})
export class CurrenciesComponent implements OnInit {
    currencies: DisplayCurrencyList = { currencies: [] };
    currencyIds: number[] = [];
    private loadingStatus: Record<number, boolean> = {};
    isLoaded: boolean = false;
    isFetching: boolean = false;
    loadingNewItems: boolean = false;
    private offset: number = 0;
    private readonly pageSize: number = 10;
    hasMore: boolean = true;
    private scrollTimer: any = null;
    constructor(
        private getAllCurrenciesService: DisplayCurrenciesService,
        private readonly log: CryptoTraderLoggerService,
    ) {}

    @HostListener('window:scroll', [])
    onScroll() {
        if (this.scrollTimer) {
            clearTimeout(this.scrollTimer);
        }
        this.scrollTimer = setTimeout(() => {
            this.scrollTimer = null;
            if (this.loadingNewItems || this.isFetching || !this.hasMore) {
                return;
            }
            const scrollPosition: number = window.scrollY + window.innerHeight;
            const pageHeight: number = document.documentElement.scrollHeight;
            if (this.shouldLoadMoreCurrencies(scrollPosition, pageHeight)) {
                this.triggerLoadMoreCurrencies();
            }
        }, 150);
    }

    onError(event: Event) {
        if (event instanceof ErrorEvent) {
            this.log.error(`Currency component error: ${event.message}`, undefined, event.message);
        }
    }

    ngOnInit(): void {
        this.log.setContext(LoggerContext.Currencies);
        this.initializeCurrencies();
        this.loadCurrencies(0);
    }

    listenForCurrencies(): void {
        this.getAllCurrenciesService
            .getAllCurrenciesWithOffset(0)
            .subscribe((data: DisplayCurrencyList) => {
                this.currencies = data;
            });
    }

    private loadCurrencies(offset: number): void {
        this.log.debug(`Loading currencies with offset ${offset}`);
        this.isFetching = true;
        this.getAllCurrenciesService
            .getAllCurrenciesWithOffset(offset)
            .subscribe({
                next: (data: DisplayCurrencyList): void => {
                    this.log.info(`Fetched ${data?.currencies?.length || 0} currencies`);
                    // append results
                    this.currencies.currencies = [
                        ...(this.currencies.currencies || []),
                        ...(data?.currencies || []),
                    ];
                    const received: number = data?.currencies?.length || 0;
                    if (received === this.pageSize) {
                        this.offset += this.pageSize;
                    } else {
                        // no more data to load
                        this.log.debug('No more currencies to load');
                        this.hasMore = false;
                    }
                    this.isLoaded = true;
                    this.isFetching = false;
                },
                error: (error): void => {
                    this.log.error('Failed to load currencies', error);
                    this.isFetching = false;
                },
            });
    }

    shouldLoadMoreCurrencies(
        scrollPosition: number,
        pageHeight: number,
    ): boolean {
        return scrollPosition >= pageHeight - 100;
    }

    private triggerLoadMoreCurrencies(): void {
        if (this.isFetching) {
            return;
        }
        this.isFetching = true;
        this.loadCurrencies(this.offset);
    }

    private loadMoreCurrencies(): void {
        this.loadingNewItems = true;
        setTimeout((): void => {
            let nextCurrencyId: number;
            if (this.currencies.currencies.length > 0) {
                nextCurrencyId = Math.max(...this.currencyIds) + 1;
            } else {
                nextCurrencyId = 0;
            }
            for (let i: number = 0; i < 5; i++) {
                const currencyId: number = nextCurrencyId + i;
                this.currencyIds.push(currencyId);
                this.loadingStatus[currencyId] = false;
            }
            this.loadingNewItems = false;
        }, 1000);
    }

    onCurrencyLoaded(currencyId: number): void {
        if (this.loadingStatus.hasOwnProperty(currencyId)) {
            this.loadingStatus[currencyId] = true;
        }
        if (this.allCurrenciesLoaded() && this.isFetching) {
            this.isFetching = false;
        }
        if (!this.isLoaded && this.initialCurrenciesLoaded()) {
            this.isLoaded = true;
        }
    }

    private allCurrenciesLoaded(): boolean {
        return Object.values(this.loadingStatus).every(
            (value: boolean): boolean => value,
        );
    }

    private initialCurrenciesLoaded(): boolean {
        const initialIds: number[] = this.currencyIds.slice(0, 10);
        return initialIds.every(
            (id: number): boolean => this.loadingStatus[id],
        );
    }

    private initializeCurrencies(): void {
        for (let i: number = 0; i < 10; i++) {
            this.currencyIds.push(i);
            this.loadingStatus[i] = false;
        }
    }
}
