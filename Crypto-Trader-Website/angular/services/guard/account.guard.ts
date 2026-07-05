import { Injectable } from '@angular/core'
import {
    ActivatedRouteSnapshot,
    CanActivate,
    GuardResult,
    MaybeAsync,
    Router,
    RouterStateSnapshot,
} from '@angular/router'
import { catchError, map, Observable, Subject, Subscriber } from 'rxjs'

import { LoggedInService } from '@http/auth/status/logged-in.service'
import { AuthResponse } from '@models/auth/types'

/**
 * Watches for account redirects.
 */
@Injectable({
    providedIn: 'root',
})
export class AccountGuard implements CanActivate {
    private readonly accountRedirect$: Subject<undefined> = new Subject<undefined>()

    constructor(
        private readonly loggedInService: LoggedInService,
        private readonly router: Router,
    ) {}

    /**
     * Gets the account redirect subject.
     * @returns The account redirect subject.
     */
    public getAccountRedirect(): Subject<undefined> {
        return this.accountRedirect$
    }

    /**
     * Checks if the user is authorized to access the account page.
     *
     * @param _route
     * @param _state
     * @returns The authorization result.
     */
    public canActivate(
        _route: ActivatedRouteSnapshot,
        _state: RouterStateSnapshot,
    ): MaybeAsync<GuardResult> {
        return this.loggedInService.isLoggedIn().pipe(
            map((authResponse: AuthResponse): boolean => {
                if (authResponse.authorized) {
                    this.accountRedirect$.next(undefined)
                    void this.router.navigate(['/account'])
                    return false
                } else {
                    return true
                }
            }),
            catchError((): Observable<boolean> => {
                void this.router.navigate(['/authorize']).then((_navigated: boolean): void => {})
                return new Observable<boolean>((observer: Subscriber<boolean>): void => {
                    observer.next(false)
                })
            }),
        )
    }
}
