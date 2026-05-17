import { CommonModule } from '@angular/common'
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { AngularSuiteModule } from '@theoliverlear/angular-suite'

import { UniversalModule } from '../shared/universal.module'
import { TerminalComponent } from 'angular/components/elements/element-group-console/terminal/terminal.component'
import { ConsoleComponent } from 'angular/components/pages/console/console.component'

// Wide range of types for components. Hard to find a common base, so we
// suppress the type definition warning.
// eslint-disable-next-line @typescript-eslint/typedef
const consoleComponents = [TerminalComponent, ConsoleComponent]

/**
 * Module for console-related components and features.
 */
@NgModule({
    declarations: [...consoleComponents],
    imports: [CommonModule, FormsModule, AngularSuiteModule, UniversalModule],
    exports: [...consoleComponents],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ConsoleModule {}
