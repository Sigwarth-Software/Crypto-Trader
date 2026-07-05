import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { AngularSuiteModule } from '@theoliverlear/angular-suite';
import { BaseChartDirective } from 'ng2-charts';

import { LineChartComponent } from '@components/elements/element-group-chart/line-chart/line-chart.component';
import { LiveChartComponent } from '@components/elements/element-group-chart/live-chart/live-chart.component';
import { SparklineComponent } from '@components/elements/element-group-chart/sparkline-chart/sparkline-chart.component';

const chartComponents = [
    LineChartComponent,
    LiveChartComponent,
    SparklineComponent,
];

@NgModule({
    declarations: [...chartComponents],
    imports: [
        CommonModule,
        BaseChartDirective,
        AngularSuiteModule,
    ],
    exports: [...chartComponents],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ChartModule {}
