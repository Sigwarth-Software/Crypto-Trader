// module-card.component.ts
import { Component, Input } from '@angular/core'
import { ModuleInfo } from '@models/module/ModuleInfo'

/**
 * A component that displays a card for a module.
 */
@Component({
    selector: 'module-card',
    standalone: false,
    templateUrl: './module-card.component.html',
    styleUrls: ['./module-card.component.scss'],
})
export class ModuleCardComponent {
    @Input() public module!: ModuleInfo
    // TODO: Extract to type. Perhaps add theme service.
    @Input() public theme: 'dark' | 'light' = 'dark'
}
