// health-time-selector.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { HealthStatusTimeRange } from '@models/health/HealthStatusTimeRange';
import { healthChartTimeRanges } from '@assets/health-chart.assets';
import { type HealthChartTimeRanges } from '@models/health/types';

/** Selects one availability range for every module on the health page. */
@Component({
    selector: 'health-time-selector',
    templateUrl: './health-time-selector.component.html',
    styleUrls: ['./health-time-selector.component.scss'],
    standalone: false
})
export class HealthTimeSelectorComponent {
    @Input() public timeRange: HealthStatusTimeRange = HealthStatusTimeRange.Quarter;
    @Output() public readonly timeRangeChange: EventEmitter<HealthStatusTimeRange> =
        new EventEmitter<HealthStatusTimeRange>();

    protected readonly ranges: HealthStatusTimeRange[] = Object.values(HealthStatusTimeRange);
    protected readonly rangeOptions: HealthChartTimeRanges = healthChartTimeRanges;
}
