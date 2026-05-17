import { Component, OnInit } from '@angular/core'

import { TextElementLink } from '@theoliverlear/angular-suite'
import {
    navBarCurrenciesTextLink,
    navBarHomeLink,
    navBarPortfolioTextLink,
    navBarTraderTextLink,
} from '@assets/elementLinkAssets'
import { LoggedInService } from '@http/auth/status/logged-in.service'

import { NavBarItemOption } from '../nav-bar-item/models/NavBarItemOption'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'

/** A navigation bar that contains links to different pages.
 *
 */
@Component({
    selector: 'nav-bar',
    standalone: false,
    templateUrl: './nav-bar.component.html',
    styleUrls: ['./nav-bar.component.scss'],
})
export class NavBarComponent implements OnInit {
    protected isLoggedIn: boolean = false
    constructor(
        private readonly loggedInService: LoggedInService,
        private readonly log: CryptoTraderLoggerService,
    ) {}

    /** On init, listen for auth status changes and verify login status.
     *
     */
    public ngOnInit(): void {
        this.log.setContext('NavBar')
        this.log.info('NavBar component initialized')
        this.listenForAuthStatus()
        this.verifyLoginStatus()
    }

    private listenForAuthStatus(): void {
        this.loggedInService.getAuthState().subscribe((authStatus: boolean): void => {
            this.log.debug(`Auth status changed: ${authStatus}`)
            this.isLoggedIn = authStatus
        })
    }

    // TODO: Add more robust options filters.
    protected shouldShowNavItem(navBarItemOption: NavBarItemOption): boolean {
        return this.isLoggedIn
    }

    /** Verify login status by subscribing to the logged in service.
     *
     */
    public verifyLoginStatus(): void {
        this.log.debug('Verifying login status...')
        this.loggedInService.isLoggedIn().subscribe()
    }

    protected readonly navBarHomeLink: TextElementLink = navBarHomeLink
    protected readonly navBarPortfolioLink: TextElementLink = navBarPortfolioTextLink
    protected readonly navBarTraderLink: TextElementLink = navBarTraderTextLink
    protected readonly navBarCurrenciesLink: TextElementLink = navBarCurrenciesTextLink
    protected readonly NavBarItemOption: typeof NavBarItemOption = NavBarItemOption
}
