import {
    HealthChartComponent
} from '@components/elements/element-group-health/health-chart/health-chart.component'
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core'
import {
    HealthTimeSelectorComponent
} from '@components/elements/element-group-health/health-time-selector/health-time-selector.component'

const healthComponents = [
    HealthChartComponent,
    HealthTimeSelectorComponent
]

@NgModule({
    declarations: [
        ...healthComponents
    ],
    exports: [
        ...healthComponents
    ],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ]
})
export class HealthModule {}