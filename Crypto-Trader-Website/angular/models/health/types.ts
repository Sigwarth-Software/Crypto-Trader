import { type HealthStatusTimeRange } from './HealthStatusTimeRange';
import { type ServiceStatus } from './ServiceStatus';

export type HealthChartTimeRange = {
    label: string;
    description: string;
    startLabel: string;
    endLabel: string;
    intervals: number;
    hourly: boolean;
}

export type HealthChartTimeRanges = Record<HealthStatusTimeRange, HealthChartTimeRange>

export type HealthChartStatusLabels = Record<ServiceStatus, string>
