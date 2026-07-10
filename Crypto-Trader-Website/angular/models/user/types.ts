// TODO: Refactor to enum.

export type SubscriptionTier = 'FREE' | 'PRO' | 'ULTIMATE'

export type SubscriptionTierResponse = {
    subscriptionTier: SubscriptionTier
}