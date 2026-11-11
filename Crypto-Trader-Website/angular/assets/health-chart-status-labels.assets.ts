import { type HealthChartStatusLabels } from '@models/health/types'
import { ServiceStatus } from '@models/health/ServiceStatus'

export const healthChartStatusLabels: HealthChartStatusLabels = {
    [ServiceStatus.ALIVE]: 'Operational',
    [ServiceStatus.DEAD]: 'Outage',
    [ServiceStatus.DEGRADED]: 'Degraded performance',
    [ServiceStatus.UNKNOWN]: 'No data',
    [ServiceStatus.MAINTENANCE]: 'Maintenance',
}

