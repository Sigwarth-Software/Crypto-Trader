import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Data, NavigationEnd, Router } from '@angular/router';
import { filter, map, mergeMap, Observable } from 'rxjs';

import { DelayService } from '@theoliverlear/angular-suite';
import { AuthGuard } from '@guards/auth.guard';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';

import { NavBarComponent } from '../elements/element-group-nav/nav-bar/nav-bar.component';
import { Meta } from '@models/html/types'

/** The root component of the application.
 *
 */
@Component({
    selector: 'app',
    standalone: false,
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
    protected showAuthGuardPopup: boolean = false;
    protected showNavBar: boolean = true;
    protected title: string;
    @ViewChild(NavBarComponent) private readonly navBar: NavBarComponent;

    constructor(
        private readonly router: Router,
        private readonly activatedRoot: ActivatedRoute,
        private readonly authGuard: AuthGuard,
        private readonly delayService: DelayService,
        private readonly logger: CryptoTraderLoggerService,
    ) {}

    /** Updates the nav bar with certain content if a user is logged in.
     *
     */
    private updateNavBar(): void {
        this.navBar.verifyLoginStatus();
    }

    /** Setup router and popups on init.
     *
     */
    public ngOnInit(): void {
        this.logger.info('AppComponent initialized.', 'App');
        this.router.events
            .pipe(
                filter(
                    // eslint-disable-next-line @typescript-eslint/typedef
                    (event): event is NavigationEnd => event instanceof NavigationEnd,
                ),
                map((): ActivatedRoute => this.activatedRoot),
                map((route: ActivatedRoute): ActivatedRoute => {
                    while (route.firstChild) {
                        route = route.firstChild;
                    }
                    return route;
                }),
                mergeMap((route: ActivatedRoute): Observable<Data> => route.data),
            )
            .subscribe((data: Data): void => {
                const metaInfo: Meta = (data['meta'] || {}) as Meta;
                this.title = metaInfo['title'] || 'Crypto Trader';
                this.showNavBar = metaInfo['showNavBar'] !== false;
                this.logger.debug(`Route changed: ${this.title}, showNavBar: ${this.showNavBar}`, 'App');
                this.updateNavBar();
            });
        this.authGuard
            .getAuthBlocked()
            /* eslint-disable-next-line @typescript-eslint/no-unused-vars */
            .subscribe((authBlocked: undefined): void => {
                void this.triggerAuthPopup();
            });
    }

    /** Triggers the auth guard popup to alert the user that they need to login.
     *
     */
    private async triggerAuthPopup(): Promise<void> {
        // TODO: Implement a navigation history service to make this easier
        //       and more correct for pages which intend for the popup.
        const currentUrl: string = this.router.url;
        this.logger.debug(`Current URL: ${currentUrl}`, 'App');
        if (!currentUrl.includes('/authorize') && !currentUrl.includes('/account') && currentUrl !== '/') {
            this.logger.info('Triggering auth guard popup.', 'App');
            this.showAuthGuardPopup = true;
            await this.delayService.delay(4200);
            this.showAuthGuardPopup = false;
        }
    }
}
