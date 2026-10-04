// page-title-stripe.component.ts
import { Component, Input } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'
import { PageTitleStripe } from '@components/elements/element-group-system/page-title-stripe/models/PageTitleStripe'

/**
 * A stripe component that displays the title of a page.
 */
@Component({
    selector: 'page-title-stripe',
    templateUrl: './page-title-stripe.component.html',
    styleUrls: ['./page-title-stripe.component.scss'],
    standalone: false,
})
export class PageTitleStripeComponent {
    @Input() public titleStripe: PageTitleStripe
    constructor() {}

    protected readonly TagType: typeof TagType = TagType
}
