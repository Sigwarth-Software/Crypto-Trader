import {
    HealthChartComponent
} from '@components/elements/element-group-health/health-chart/health-chart.component'
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core'

const healthComponents = [
    HealthChartComponent
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