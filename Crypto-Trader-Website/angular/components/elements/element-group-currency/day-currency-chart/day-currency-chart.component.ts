// day-currency-chart.component.ts
import { Component, Input } from '@angular/core';

import { type ChartConfig, type ChartDataPoint } from '@models/chart/types';

/** A chart displaying the daily price of a currency.
 *
 */
@Component({
    selector: 'day-currency-chart',
    templateUrl: './day-currency-chart.component.html',
    styleUrls: ['./day-currency-chart.component.scss'],
    standalone: false,
})
export class DayCurrencyChartComponent {
    @Input() public config: ChartConfig;
    @Input() public data: ChartDataPoint[] = [];

    constructor() {}
}
