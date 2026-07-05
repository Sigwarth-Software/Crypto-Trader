import { HttpClientService } from '@theoliverlear/angular-suite'
import { PossibleSubscriptionTierPrices, SubscriptionTierPrices } from '@models/promo/types'
import { environment } from '@environments/environment'
import { Injectable } from '@angular/core'
import { BehaviorSubject, Observable } from 'rxjs'
import { map } from 'rxjs/operators'

@Injectable({
    providedIn: 'root',
})
export class SubscriptionTierPricesService extends HttpClientService<
    never,
    SubscriptionTierPrices
> {
    private static readonly URL: string = `${environment.apiUrl}/subscription-tier/prices`
    private readonly subscriptionTierPricesState$: BehaviorSubject<PossibleSubscriptionTierPrices> =
        new BehaviorSubject<PossibleSubscriptionTierPrices>(null)
    constructor() {
        super(SubscriptionTierPricesService.URL)
    }

    getTierPrices(): Observable<SubscriptionTierPrices> {
        return this.get().pipe(
            map((response: SubscriptionTierPrices): SubscriptionTierPrices => {
                this.subscriptionTierPricesState$.next(response)
                return response
            })
        )
    }

    getTierPricesState(): Observable<PossibleSubscriptionTierPrices> {
        return this.subscriptionTierPricesState$.asObservable()
    }
}