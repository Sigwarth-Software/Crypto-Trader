export enum SubscriptionTier {
    Free = 'Free Tier',
    Pro = 'Pro Tier',
    Ultimate = 'Ultimate Tier',
}
export namespace SubscriptionTier {
    export function values(): SubscriptionTier[] {
        return Object.values(SubscriptionTier) as SubscriptionTier[]
    }
}
