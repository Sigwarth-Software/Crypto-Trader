import { Component, OnInit } from '@angular/core'
import { CryptoTraderLoggerService } from '@app/services/logging/crypto-trader-logger.service'

import { LoggedInService } from '@http/auth/status/logged-in.service'
import { SubscriptionTierService } from '@http/user/subscription-tier.service'
import { SubscriptionTierResponse } from '@models/user/types'
import { LoggerContext } from '@models/logging/LoggerContext'

/**
 * The nav items at the end of the nav bar.
 */
@Component({
    selector: 'nav-bumper',
    templateUrl: './nav-bumper.component.html',
    styleUrls: ['./nav-bumper.component.scss'],
    standalone: false,
})
export class NavBumperComponent implements OnInit {
    protected isLoggedIn: boolean = false
    protected isUltimate: boolean = false
    constructor(
        private readonly loggedInService: LoggedInService,
        private readonly mySubscriptionTierService: SubscriptionTierService,
        private readonly logger: CryptoTraderLoggerService,
    ) {}

    /**
     * On init, set the logger context and listen for auth status and subscription tier changes.
     */
    public ngOnInit(): void {
        this.logger.setContext(LoggerContext.Navigation)
        this.listenForAuthStatus()
        this.listenForSubscriptionTier()
    }

    private listenForAuthStatus(): void {
        this.loggedInService.getAuthState().subscribe((authStatus: boolean): void => {
            this.isLoggedIn = authStatus
        })
    }

    private listenForSubscriptionTier(): void {
        this.mySubscriptionTierService
            .getSubscriptionTier()
            .subscribe((tier: SubscriptionTierResponse): void => {
                this.logger.log(`Observed subscription tier: ${tier.subscriptionTier}`)
                // TODO: This should be an enum.
                this.isUltimate = tier.subscriptionTier === 'ULTIMATE'
            })
    }
}
