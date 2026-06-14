import { Injectable } from '@angular/core'
import { BehaviorSubject, map, Observable } from 'rxjs'

import { HttpClientService } from '@theoliverlear/angular-suite'
import { environment } from '@environments/environment'
import { TokenStorageService } from '@auth/token-storage.service'
import { AuthResponse, PossibleToken } from '@models/auth/types'

/**
 * Lightweight auth status client for the UI.
 *
 * - Calls GET /auth/logged-in to check whether the current request is authenticated.
 * - Does not send Authorization/DPoP headers; the endpoint is public and simply reflects server state.
 * - If the server returns authorized=true but no token (which is normal), we enrich the response with the
 *   current in-memory token so components that need it don’t have to query storage separately.
 */
@Injectable({
    providedIn: 'root',
})
export class LoggedInService extends HttpClientService<never, AuthResponse> {
    private static readonly URL: string = `${environment.apiUrl}/auth/logged-in`
    private readonly authState$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false)

    constructor(private readonly tokenStorage: TokenStorageService) {
        super(LoggedInService.URL)
    }

    /**
     * Query the server for current auth status.
     * Returns an AuthResponse; if authorized=true and no token is present, we attach the in-memory token for convenience.
     */
    public isLoggedIn(): Observable<AuthResponse> {
        return this.get().pipe(
            map((response: AuthResponse): AuthResponse => {
                const isAuthorized: boolean = response?.authorized || false
                this.authState$.next(isAuthorized)
                if (isAuthorized) {
                    if (response.token) {
                        return { ...response, token: response.token }
                    }
                    const token: PossibleToken = this.tokenStorage.getToken()
                    return token ? { ...response, token } : response
                }
                return response
            }),
        )
    }

    /**
     * Retrieves the recent authentication state.
     */
    public getAuthState(): Observable<boolean> {
        return this.authState$.asObservable()
    }
}
