// module-interface-selector.component.ts
import { Component, Input } from '@angular/core'
import { ModuleInfo } from '@models/module/ModuleInfo'

/**
 * A component that allows users to select a module interface.
 */
@Component({
    selector: 'module-interface-selector',
    standalone: false,
    templateUrl: './module-interface-selector.component.html',
    styleUrls: ['./module-interface-selector.component.scss'],
})
export class ModuleInterfaceSelectorComponent {
    @Input() public modules: ModuleInfo[] = []

    protected activeInterface: string = 'website'

    /**
     * Sets the active interface to the specified name.
     *
     * @param name
     */
    public selectInterface(name: string): void {
        this.activeInterface = name.toLowerCase()
    }

    /**
     * Returns the currently active interface module info.
     *
     * @returns The active interface module info, or undefined if not found.
     */
    public getActiveInterface(): ModuleInfo | undefined {
        return this.modules.find(
            (moduleInfo: ModuleInfo): boolean =>
                moduleInfo.name.toLowerCase() === this.activeInterface,
        )
    }
}
