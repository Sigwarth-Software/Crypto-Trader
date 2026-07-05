// tier-promo-stripe.component.ts
import { Component, OnInit } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'

import { SubscriptionTier } from '../tier-promo/models/SubscriptionTier'
import { SubscriptionTierPricesService } from '@http/subscription-tier/subscription-tier-prices.service'
import { SubscriptionTierPrices } from '@models/promo/types'

/**
 * A stripe promoting subscription tiers
 */
@Component({
    selector: 'tier-promo-stripe',
    standalone: false,
    templateUrl: './tier-promo-stripe.component.html',
    styleUrls: ['./tier-promo-stripe.component.scss'],
})
export class TierPromoStripeComponent implements OnInit {
    protected subscriptionTierPrices: SubscriptionTierPrices
    constructor(private readonly subscriptionTierPricesService: SubscriptionTierPricesService) {}

    /**
     * Initializes prices on component initialization.
     */
    public ngOnInit(): void {
        this.subscriptionTierPricesService
            .getTierPrices()
            .subscribe((prices: SubscriptionTierPrices): void => {
                this.subscriptionTierPrices = prices
            })
    }

    protected readonly SubscriptionTier: typeof SubscriptionTier = SubscriptionTier
    protected readonly TagType: typeof TagType = TagType
}
