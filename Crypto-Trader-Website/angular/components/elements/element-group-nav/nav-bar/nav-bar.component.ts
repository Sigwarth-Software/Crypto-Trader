import { Component, HostBinding, OnChanges, OnInit, SimpleChanges } from '@angular/core'

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
export class NavBarComponent implements OnInit, OnChanges {
    /**
     * Listens for auth status changes to update nav bar styling.
     * @returns Whether the user is logged in.
     */
    @HostBinding('class.logged-in') public get loggedInStyle(): boolean {
        return this.isLoggedIn
    }
    protected isLoggedIn: boolean = false
    constructor(
        private readonly loggedInService: LoggedInService,
        private readonly log: CryptoTraderLoggerService,
    ) {}

    /**
     * On changes, make sure the login status is up to date.
     * @param simpleChanges
     */
    public ngOnChanges(simpleChanges: SimpleChanges): void {
        if ('isLoggedIn' in simpleChanges) {
            this.isLoggedIn = Boolean(simpleChanges.isLoggedIn.currentValue)
        }
    }

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
        if (
            navBarItemOption === NavBarItemOption.Simulator ||
            navBarItemOption === NavBarItemOption.Currencies
        ) {
            return true
        }
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
