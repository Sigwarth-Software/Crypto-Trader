// health.component.ts
import { Component } from '@angular/core';
import { healthPageTitleStripe } from '@assets/page-title-stripe.assets'
import { CryptoTraderModules } from '@models/health/CryptoTraderHealthModules'
import { HealthStatusTimeRange } from '@models/health/HealthStatusTimeRange';
import { healthChartTimeRanges } from '@assets/health-chart.assets';
import { type HealthChartTimeRanges } from '@models/health/types';
import {
    PageTitleStripe
} from '@components/elements/element-group-system/page-title-stripe/models/PageTitleStripe'

@Component({
    selector: 'health',
    templateUrl: './health.component.html',
    styleUrls: ['./health.component.scss'],
    standalone: false
})
export class HealthComponent {
    protected timeRange: HealthStatusTimeRange = HealthStatusTimeRange.Quarter;
    protected readonly rangeOptions: HealthChartTimeRanges = healthChartTimeRanges;
    protected readonly modules: CryptoTraderModules[] = Object.values(CryptoTraderModules)
    constructor() {

    }

    protected readonly healthPageTitleStripe: PageTitleStripe = healthPageTitleStripe
}
