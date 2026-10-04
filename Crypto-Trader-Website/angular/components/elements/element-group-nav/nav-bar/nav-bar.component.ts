import { Component, HostBinding, OnChanges, OnInit, SimpleChanges } from '@angular/core'

import { TextElementLink } from '@theoliverlear/angular-suite'
import { navBarHomeLink } from '@assets/element-link.assets'
import { LoggedInService } from '@http/auth/status/logged-in.service'

import { NavBarItemOption } from '../nav-bar-item/models/NavBarItemOption'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { LoggerContext } from '@models/logging/LoggerContext'

/**
 * A navigation bar that contains links to different pages.
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
        private readonly logger: CryptoTraderLoggerService,
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

    /**
     * On init, listen for auth status changes and verify login status.
     */
    public ngOnInit(): void {
        this.logger.setContext(LoggerContext.Navigation)
        this.listenForAuthStatus()
        this.verifyLoginStatus()
    }

    private listenForAuthStatus(): void {
        this.loggedInService.getAuthState().subscribe((authStatus: boolean): void => {
            this.logger.debug(`Auth status changed: ${authStatus}`)
            this.isLoggedIn = authStatus
        })
    }

    // TODO: Add more robust options filters.
    protected shouldShowNavItem(navBarItemOption: NavBarItemOption): boolean {
        const NAV_ITEMS_TO_SHOW_WHEN_LOGGED_OUT: NavBarItemOption[] = [
            NavBarItemOption.Simulator,
            NavBarItemOption.Currencies,
        ]
        if (NAV_ITEMS_TO_SHOW_WHEN_LOGGED_OUT.includes(navBarItemOption)) {
            return true
        }
        return this.isLoggedIn
    }

    /**
     * Verify login status by subscribing to the logged-in service.
     */
    public verifyLoginStatus(): void {
        this.logger.debug('Verifying login status...')
        this.loggedInService.isLoggedIn().subscribe()
    }

    protected readonly navBarHomeLink: TextElementLink = navBarHomeLink
    protected readonly NavBarItemOption: typeof NavBarItemOption = NavBarItemOption
}
