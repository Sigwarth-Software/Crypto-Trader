import { Injectable } from '@angular/core';
import {
    ActivatedRouteSnapshot,
    CanActivate,
    type GuardResult,
    type MaybeAsync,
    Router,
    RouterStateSnapshot,
} from '@angular/router';
import { catchError, map, Observable, Subject } from 'rxjs';

import { LoggedInService } from '@http/auth/status/logged-in.service';

@Injectable({
    providedIn: 'root',
})
export class DashboardGuard implements CanActivate {
    dashboardRedirect$: Subject<undefined> = new Subject<undefined>();

    constructor(
        private router: Router,
        private loggedInService: LoggedInService,
    ) {}

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot,
    ): MaybeAsync<GuardResult> {
        return this.loggedInService.isLoggedIn().pipe(
            map((authResponse) => {
                if (authResponse.authorized) {
                    this.dashboardRedirect$.next(undefined);
                    this.router.navigate(['/dashboard']);
                    return false;
                } else {
                    return true;
                }
            }),
            catchError(() => {
                this.router.navigate(['/authorize']).then((navigated) => {});
                return new Observable<boolean>((observer) => {
                    observer.next(false);
                });
            }),
        );
    }
}
