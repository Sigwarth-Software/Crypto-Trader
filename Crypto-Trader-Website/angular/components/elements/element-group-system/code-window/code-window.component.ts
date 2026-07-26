// code-window.component.ts
import { Component, Input } from '@angular/core'
import { CodeWindow } from '@models/promo/types'

/**
 * A component for displaying a code window.
 */
@Component({
    selector: 'code-window',
    standalone: false,
    templateUrl: './code-window.component.html',
    styleUrls: ['./code-window.component.scss'],
})
export class CodeWindowComponent {
    @Input() public codeWindow: CodeWindow
}
