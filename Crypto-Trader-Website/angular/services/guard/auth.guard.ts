import { Injectable } from '@angular/core'
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router'
import { catchError, map, Observable, Subject, Subscriber } from 'rxjs'

import { LoggedInService } from '@http/auth/status/logged-in.service'
import { AuthResponse } from '@models/auth/types'

/**
 * Guards routes against unauthorized access.
 */
@Injectable({
    providedIn: 'root',
})
/**
 * Route guard that checks server-side auth status before activating a protected route.
 *
 * Behavior:
 * - Calls LoggedInService.isLoggedIn() (GET /auth/logged-in) to determine whether the current request is authenticated.
 * - If authorized, allows activation. Otherwise, redirects to /authorize and blocks activation.
 * - It does not attach Authorization/DPoP itself; the endpoint is designed to be public and reflect state.
 */
export class AuthGuard implements CanActivate {
    private readonly authBlocked$: Subject<void> = new Subject<void>()
    constructor(
        private readonly router: Router,
        private readonly loggedInService: LoggedInService,
    ) {}

    /**
     * Gets the auth-blocked subject.
     * @returns The auth blocked subject.
     */
    public getAuthBlocked(): Observable<void> {
        return this.authBlocked$.asObservable()
    }

    /**
     * Allows route when authenticated.
     * @param _route
     * @param _state
     *
     * @returns True if the route can be activated, false otherwise.
     */
    public canActivate(
        _route: ActivatedRouteSnapshot,
        _state: RouterStateSnapshot,
    ): Observable<boolean> {
        return this.loggedInService.isLoggedIn().pipe(
            map((authResponse: AuthResponse): boolean => {
                if (authResponse.authorized) {
                    return true
                } else {
                    this.authBlocked$.next(undefined)
                    void this.router.navigate(['/authorize'])
                    return false
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
