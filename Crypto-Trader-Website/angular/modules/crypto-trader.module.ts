/**
 * Angular root module — a clean composition of feature modules.
 *
 * Bootstrap logic, singleton services, and HTTP interceptors are provided by
 * CryptoTraderCoreModule. Feature modules own their component declarations.
 * This module declares only root-level pages that have no feature affiliation.
 */
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterOutlet } from '@angular/router';

import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import { AppComponent } from '@components/app/app.component';
import { elements } from '../components/elements/elements';
import { pages } from '../components/pages/pages';

import { AppRouting } from './routing/app-routing.module';
import { CryptoTraderCoreModule } from './core/crypto-trader-core.module';
import { UniversalModule } from './shared/universal.module';
import { ChartModule } from './shared/chart.module';
import { AuthModule } from './feature/auth.module';
import { ChatModule } from './feature/chat.module';
import { ConsoleModule } from './feature/console.module';
import { CurrencyModule } from './feature/currency.module';
import { DashboardModule } from './feature/dashboard.module';
import { ModulesModule } from './feature/modules.module';
import { NavModule } from './feature/nav.module';
import { PortfolioModule } from './feature/portfolio.module';
import { PromoModule } from './feature/promo.module';
import { TradeModule } from './feature/trade.module';
import { TraderModule } from './feature/trader.module';
import { AngularSuiteModule } from '@theoliverlear/angular-suite';
import {LoggerContext} from "@models/logging/LoggerContext";

@NgModule({
    declarations: [AppComponent, ...elements, ...pages],
    imports: [
        CommonModule,
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        AppRouting,
        RouterOutlet,
        HttpClientModule,
        CryptoTraderCoreModule,
        AngularSuiteModule,
        UniversalModule,
        ChartModule,
        AuthModule,
        ChatModule,
        ConsoleModule,
        CurrencyModule,
        DashboardModule,
        ModulesModule,
        NavModule,
        PortfolioModule,
        PromoModule,
        TradeModule,
        TraderModule,
    ],
    bootstrap: [AppComponent],
    exports: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class CryptoTraderModule {
    constructor(private readonly logger: CryptoTraderLoggerService) {
        this.logger.setContext(LoggerContext.System)
        this.logger.info(`Crypto-Trader-Website initialized.`)
    }
}
