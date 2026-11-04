// health-chart.component.ts
import { Component, Input, type OnChanges, type OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { HealthStatusTimeRange } from '@models/health/HealthStatusTimeRange';
import { healthChartStatusLabels } from '@assets/health-chart-status-labels.assets';
import { type HealthChartStatusLabels, type HealthChartTimeRange } from '@models/health/types';
import { CryptoTraderModules } from '@models/health/CryptoTraderHealthModules';
import { ServiceStatus } from '@models/health/ServiceStatus';
import { ServiceStatusService } from '@http/health/service-status.service';
import { healthChartTimeRanges } from '@assets/health-chart-time-ranges.assets'

@Component({
    selector: 'health-chart',
    templateUrl: './health-chart.component.html',
    styleUrls: ['./health-chart.component.scss'],
    standalone: false
})
export class HealthChartComponent implements OnChanges, OnDestroy {
    @Input({ required: true }) public serviceName!: CryptoTraderModules;
    @Input() public timeRange: HealthStatusTimeRange = HealthStatusTimeRange.Quarter;

    protected range: HealthChartTimeRange =
        healthChartTimeRanges[HealthStatusTimeRange.Quarter];
    protected days: string[] = this.buildIntervals();
    protected statuses: ServiceStatus[] = [];
    protected loading: boolean = false;
    protected loadError: boolean = false;
    protected readonly statusLabels: HealthChartStatusLabels = healthChartStatusLabels;
    private request?: Subscription;

    constructor(private readonly serviceStatusService: ServiceStatusService) {}

    /** Reloads this module when either input changes. */
    public ngOnChanges(): void {
        this.loadStatuses();
    }

    /** Cancels any outstanding request when this chart is removed. */
    public ngOnDestroy(): void {
        this.request?.unsubscribe();
    }

    /** Loads the selected range, cancelling a previous request before replacing its state. */
    protected loadStatuses(): void {
        this.request?.unsubscribe();
        this.range = healthChartTimeRanges[this.timeRange];
        this.days = this.buildIntervals();
        this.statuses = [];
        this.loading = true;
        this.loadError = false;
        this.request = this.serviceStatusService.getServiceStatus(this.serviceName, this.timeRange)
            .subscribe({
                next: (statuses: ServiceStatus[]): void => {
                    this.statuses = statuses;
                    this.loading = false;
                },
                error: (): void => {
                    this.loadError = true;
                    this.loading = false;
                },
            });
    }

    protected get latestStatus(): ServiceStatus {
        return this.statusAt(this.days.length - 1);
    }

    protected get summary(): string {
        if (this.loading) {
            return 'Loading…';
        }
        if (this.loadError) {
            return 'Unable to load history';
        }
        return this.timeRange === HealthStatusTimeRange.Now
            ? this.statusLabels[this.latestStatus]
            : `Latest interval: ${this.statusLabels[this.latestStatus]}`;
    }

    protected statusAt(index: number): ServiceStatus {
        return this.statuses[index] ?? ServiceStatus.UNKNOWN;
    }

    protected intervalDescription(index: number): string {
        if (this.loading) {
            return 'Loading';
        }
        if (this.loadError) {
            return 'History unavailable';
        }
        return this.statusLabels[this.statusAt(index)];
    }

    private buildIntervals(): string[] {
        const now: Date = new Date();
        return Array.from({ length: this.range.intervals }, (
            _day: unknown,
            index: number,
        ): string => {
            const date: Date = new Date(now);
            const offset: number = this.range.intervals - 1 - index;

            if (this.range.hourly) {
                date.setHours(date.getHours() - offset);
            } else {
                date.setDate(date.getDate() - offset);
            }
            return date.toLocaleString(undefined, {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                ...(this.range.hourly || this.range.intervals === 1
                    ? { hour: 'numeric', minute: '2-digit' } as const
                    : {}),
            });
        });
    }
}
