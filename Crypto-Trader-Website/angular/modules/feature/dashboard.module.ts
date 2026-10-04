import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { AngularSuiteModule } from '@theoliverlear/angular-suite';

import { UniversalModule } from '../shared/universal.module';
import { AllocationBadgeComponent } from '@components/elements/element-group-dashboard/allocation-badge/allocation-badge.component';
import { CurrencyTickerTileComponent } from '@components/elements/element-group-dashboard/currency-ticker-tile/currency-ticker-tile.component';
import { QuickActionCardComponent } from '@components/elements/element-group-dashboard/quick-action-card/quick-action-card.component';
import { TradeRowComponent } from '@components/elements/element-group-dashboard/trade-row/trade-row.component';
import { DashboardComponent } from '@components/pages/dashboard/dashboard.component';
import {CurrencyModule} from "@modules/feature/currency.module";

const dashboardComponents = [
    AllocationBadgeComponent,
    CurrencyTickerTileComponent,
    QuickActionCardComponent,
    TradeRowComponent,
    DashboardComponent,
];

@NgModule({
    declarations: [...dashboardComponents],
    imports: [
        CommonModule,
        RouterModule,
        BaseChartDirective,
        AngularSuiteModule,
        UniversalModule,
        CurrencyModule,
    ],
    exports: [...dashboardComponents],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class DashboardModule {}
