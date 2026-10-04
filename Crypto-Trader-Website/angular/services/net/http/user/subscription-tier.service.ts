import { Injectable } from '@angular/core';
import { HttpClientService } from '@theoliverlear/angular-suite';
import { SubscriptionTier, SubscriptionTierResponse } from '@models/user/types'
import { environment } from '@environments/environment';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

/** HTTP service that fetches the current user's subscription tier.
 *
 */
@Injectable({
    providedIn: 'root',
})
export class SubscriptionTierService extends HttpClientService<never, SubscriptionTierResponse> {
    private static readonly URL: string = `${environment.apiUrl}/user/tier`
    private readonly subscriptionTier$: BehaviorSubject<SubscriptionTierResponse> =
        new BehaviorSubject<SubscriptionTierResponse>({ subscriptionTier: 'FREE' })
    constructor() {
        super(SubscriptionTierService.URL)
    }

    /** Fetch subscription tier from API.
     * @returns The stream for fetching the subscription tier response.
     */
    public getSubscriptionTier(): Observable<SubscriptionTierResponse> {
        return this.get().pipe(
            map((response: SubscriptionTierResponse): SubscriptionTierResponse => {
                this.subscriptionTier$.next(response)
                return response
            }),
        )
    }

    /** Obtains the current subscription tier already stored.
     * @returns The stream for the current subscription tier state.
     */
    public getSubscriptionTierStream(): Observable<SubscriptionTierResponse> {
        return this.subscriptionTier$.asObservable()
    }
}
