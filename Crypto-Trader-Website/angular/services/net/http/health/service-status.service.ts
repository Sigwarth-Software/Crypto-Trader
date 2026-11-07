import { HttpClientService } from '@theoliverlear/angular-suite'
import { ServiceStatus } from '@models/health/ServiceStatus'
import { environment } from '@environments/environment'
import { Injectable } from '@angular/core'
import { CryptoTraderModules } from '@models/health/CryptoTraderHealthModules'
import { HealthStatusTimeRange } from '@models/health/HealthStatusTimeRange'
import { map, Observable } from 'rxjs'

@Injectable({
    providedIn: 'root',
})
export class ServiceStatusService extends HttpClientService<never, ServiceStatus[]> {
    private static readonly URL: string = `${environment.apiUrl}/health/`

    constructor() {
        super(ServiceStatusService.URL)
    }

    public getServiceStatus(
        module: CryptoTraderModules,
        timeRange: HealthStatusTimeRange
    ): Observable<ServiceStatus[]> {
        this.url = `${ServiceStatusService.URL}${module}/${timeRange}`
        return this.get().pipe(
            map((statuses: ServiceStatus[] | null): ServiceStatus[] => statuses ?? [])
        )
    }
}
