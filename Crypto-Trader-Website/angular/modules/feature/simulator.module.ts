import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core'
import { CommonModule } from '@angular/common'
import { FormsModule } from '@angular/forms'
import { AngularSuiteModule } from '@theoliverlear/angular-suite'
import { UniversalModule } from '@modules/shared/universal.module'
import {
    SimulatorComponent
} from '@components/pages/simulator/simulator.component'

const simulatorComponents: any[] = [
    SimulatorComponent,
]

@NgModule({
    declarations: [...simulatorComponents],
    imports: [
        CommonModule,
        FormsModule,
        AngularSuiteModule,
        UniversalModule,
    ],
    exports: [...simulatorComponents],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class SimulatorModule {}