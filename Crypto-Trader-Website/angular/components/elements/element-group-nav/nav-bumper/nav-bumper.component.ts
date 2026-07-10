import { Component, OnInit } from '@angular/core';
import { CryptoTraderLoggerService } from '@app/services/logging/crypto-trader-logger.service'

import { LoggedInService } from '@http/auth/status/logged-in.service';
import { SubscriptionTierService } from '@http/user/subscription-tier.service'
import { SubscriptionTierResponse } from '@models/user/types'
import { LoggerContext } from '@models/logging/LoggerContext'

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
        private readonly log: CryptoTraderLoggerService,
    ) {}

    public ngOnInit(): void {
        this.log.setContext(LoggerContext.Navigation)
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
                this.log.log(`Observed subscription tier: ${tier.subscriptionTier}`)
                this.isUltimate = tier.subscriptionTier === 'ULTIMATE'
        })
    }
}
