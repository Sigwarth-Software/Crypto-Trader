import type { HealthChartTimeRanges } from '@models/health/types'
import { HealthStatusTimeRange } from '@models/health/HealthStatusTimeRange'

export const healthChartTimeRanges: HealthChartTimeRanges = {
    [HealthStatusTimeRange.Now]: {
        label: 'Now', description: 'Current availability',
        startLabel: 'Current status', endLabel: 'Now', intervals: 1, hourly: false,
    },
    [HealthStatusTimeRange.Day]: {
        label: 'Day', description: 'Availability over the past 24 hours',
        startLabel: '24 hours ago', endLabel: 'Now', intervals: 24, hourly: true,
    },
    [HealthStatusTimeRange.Week]: {
        label: 'Week', description: 'Availability over the past 7 days',
        startLabel: '7 days ago', endLabel: 'Today', intervals: 7, hourly: false,
    },
    [HealthStatusTimeRange.Month]: {
        label: 'Month', description: 'Availability over the past 30 days',
        startLabel: '30 days ago', endLabel: 'Today', intervals: 30, hourly: false,
    },
    [HealthStatusTimeRange.Quarter]: {
        label: 'Quarter', description: 'Availability over the past 90 days',
        startLabel: '90 days ago', endLabel: 'Today', intervals: 90, hourly: false,
    },
};