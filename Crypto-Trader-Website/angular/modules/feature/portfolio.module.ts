import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AngularSuiteModule } from '@theoliverlear/angular-suite';
import { BaseChartDirective } from 'ng2-charts';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { UniversalModule } from '../shared/universal.module';
import { AssetButtonComponent } from 'angular/components/elements/element-group-portfolio/asset-button/asset-button.component';
import { AssetButtonsComponent } from 'angular/components/elements/element-group-portfolio/asset-buttons/asset-buttons.component';
import { AssetFieldListComponent } from 'angular/components/elements/element-group-portfolio/asset-field-list/asset-field-list.component';
import { AssetFieldComponent } from 'angular/components/elements/element-group-portfolio/asset-field/asset-field.component';
import { AssetKillSwitchComponent } from 'angular/components/elements/element-group-portfolio/asset-kill-switch/asset-kill-switch.component';
import { CurrencyWalletComponent } from 'angular/components/elements/element-group-portfolio/currency-wallet/currency-wallet.component';
import { PerformanceChartSettingsComponent } from 'angular/components/elements/element-group-portfolio/element-group-performance/performance-chart-settings/performance-chart-settings.component';
import { PerformanceChartComponent } from 'angular/components/elements/element-group-portfolio/element-group-performance/performance-chart/performance-chart.component';
import { PerformanceSectionComponent } from 'angular/components/elements/element-group-portfolio/element-group-performance/performance-section/performance-section.component';
import { PortfolioAssetListComponent } from 'angular/components/elements/element-group-portfolio/portfolio-asset-list/portfolio-asset-list.component';
import { PortfolioAssetSearchBarComponent } from 'angular/components/elements/element-group-portfolio/portfolio-asset-search-bar/portfolio-asset-search-bar.component';
import { PortfolioAssetComponent } from 'angular/components/elements/element-group-portfolio/portfolio-asset/portfolio-asset.component';
import { PortfolioAssetsReportComponent } from 'angular/components/elements/element-group-portfolio/portfolio-assets-report/portfolio-assets-report.component';
import { ChartModule } from '../shared/chart.module';
import { PortfolioInputComponent } from 'angular/components/elements/element-group-portfolio/portfolio-input/portfolio-input.component';
import { PortfolioKillSwitchComponent } from 'angular/components/elements/element-group-portfolio/portfolio-kill-switch/portfolio-kill-switch.component';
import { PortfolioOverviewComponent } from 'angular/components/elements/element-group-portfolio/portfolio-overview/portfolio-overview.component';
import { PortfolioSectionArrowComponent } from 'angular/components/elements/element-group-portfolio/portfolio-section-arrow/portfolio-section-arrow.component';
import { PortfolioSectionSelectorBubbleComponent } from 'angular/components/elements/element-group-portfolio/portfolio-section-selector-bubble/portfolio-section-selector-bubble.component';
import { PortfolioSectionSelectorComponent } from 'angular/components/elements/element-group-portfolio/portfolio-section-selector/portfolio-section-selector.component';
import { PortfolioSectionComponent } from 'angular/components/elements/element-group-portfolio/portfolio-section/portfolio-section.component';
import { PortfolioStatisticComponent } from 'angular/components/elements/element-group-portfolio/portfolio-statistic/portfolio-statistic.component';
import { PortfolioStatisticsSectionComponent } from 'angular/components/elements/element-group-portfolio/portfolio-statistics-section/portfolio-statistics-section.component';
import { PortfolioStatisticsComponent } from 'angular/components/elements/element-group-portfolio/portfolio-statistics/portfolio-statistics.component';
import { PortfolioComponent } from 'angular/components/pages/portfolio/portfolio.component';

const portfolioComponents = [
    AssetButtonComponent,
    AssetButtonsComponent,
    AssetFieldComponent,
    AssetFieldListComponent,
    AssetKillSwitchComponent,
    CurrencyWalletComponent,
    PerformanceChartComponent,
    PerformanceChartSettingsComponent,
    PerformanceSectionComponent,
    PortfolioAssetComponent,
    PortfolioAssetListComponent,
    PortfolioAssetSearchBarComponent,
    PortfolioAssetsReportComponent,
    PortfolioInputComponent,
    PortfolioKillSwitchComponent,
    PortfolioOverviewComponent,
    PortfolioSectionArrowComponent,
    PortfolioSectionComponent,
    PortfolioSectionSelectorBubbleComponent,
    PortfolioSectionSelectorComponent,
    PortfolioStatisticComponent,
    PortfolioStatisticsComponent,
    PortfolioStatisticsSectionComponent,
    PortfolioComponent,
];

@NgModule({
    declarations: [...portfolioComponents],
    imports: [
        CommonModule,
        FormsModule,
        BaseChartDirective,
        MatSlideToggleModule,
        AngularSuiteModule,
        UniversalModule,
        ChartModule,
    ],
    exports: [...portfolioComponents],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class PortfolioModule {}
