// module-architecture-map.component.ts
import { Component, Input } from '@angular/core'

// TODO: Move to models file.
/**
 * The structure of an architecture layer.
 */
export interface ArchitectureLayer {
    label: string
    // TODO: Extract type.
    nodes: { name: string; highlight?: boolean; small?: boolean }[]
}

/**
 * A component that displays a map of the architecture layers.
 */
@Component({
    selector: 'module-architecture-map',
    standalone: false,
    templateUrl: './module-architecture-map.component.html',
    styleUrls: ['./module-architecture-map.component.scss'],
})
export class ModuleArchitectureMapComponent {
    @Input() public layers: ArchitectureLayer[] = []
    @Input() public allModuleNames: string[] = []
}
