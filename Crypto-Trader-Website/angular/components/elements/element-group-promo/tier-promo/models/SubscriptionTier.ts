export enum SubscriptionTier {
    Free = 'Free Tier',
    Pro = 'Pro Tier',
    Ultimate = 'Ultimate Tier',
}
export namespace SubscriptionTier {
    export function values(): SubscriptionTier[] {
        return [
            SubscriptionTier.Free,
            SubscriptionTier.Pro,
            SubscriptionTier.Ultimate,
        ]
    }
}
