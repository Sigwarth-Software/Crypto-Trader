// cta-button.component.ts
import { Component, Input } from '@angular/core'

/**
 * A call-to-action button component.
 */
@Component({
    selector: 'cta-button',
    standalone: false,
    templateUrl: './cta-button.component.html',
    styleUrls: ['./cta-button.component.scss'],
})
export class CtaButtonComponent {
    @Input() public text: string = ''
    @Input() public routerLink: string | null = null
    @Input() public href: string | null = null
    @Input() public showArrow: boolean = true
}
