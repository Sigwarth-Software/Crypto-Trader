export type CodeWindow = {
    title: string
    code: string
}

export type SubscriptionTierPrices = {
    freeTierCost: number
    proTierCost: number
    ultimateTierCost: number
}

export type PossibleSubscriptionTierPrices = SubscriptionTierPrices | null

/**
 * A feature of a subscription tier.
 */
export interface TierFeature {
    text: string
    highlight?: boolean
}