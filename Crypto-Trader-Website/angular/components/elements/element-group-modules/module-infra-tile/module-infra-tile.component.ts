// module-infra-tile.component.ts
import { Component, Input } from '@angular/core'
import { ModuleInfo } from '@models/module/ModuleInfo'

/**
 * A component that displays a tile for a module's infrastructure.
 */
@Component({
    selector: 'module-infra-tile',
    standalone: false,
    templateUrl: './module-infra-tile.component.html',
    styleUrls: ['./module-infra-tile.component.scss'],
})
export class ModuleInfraTileComponent {
    // TODO: Remove the non-null assertion.
    @Input() public module!: ModuleInfo
}
