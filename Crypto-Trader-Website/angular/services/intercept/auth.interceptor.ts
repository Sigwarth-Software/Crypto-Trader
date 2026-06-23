/**
 * HTTP interceptor that attaches DPoP-bound Authorization headers to outgoing requests,
 * triggers proactive/reactive refresh, and ensures credentials are only sent to refresh/logout.
 */
import { type HttpEvent, type HttpInterceptorFn, type HttpRequest } from '@angular/common/http'
import { inject } from '@angular/core'
import { catchError, finalize, from, of, shareReplay, switchMap, type Observable } from 'rxjs'

import { TokenRefreshService } from '@http/auth/token/token-refresh.service'
import { DpopProofService } from '@auth/dpop/dpop-proof.service'
import { TokenStorageService } from '@auth/token-storage.service'
import { type PossibleToken } from '@models/auth/types'
import { type PossibleString } from '@models/types'
import { resolveAbsoluteHttpUrl } from '@services/net/url-resolver'

import { type PossibleStringObservable } from './types'

let refreshInFlight$: PossibleStringObservable = null

/**
 * Determine whether a JWT is expired (with small client-side skew).
 * Do not throw on parse errors; treat malformed tokens as expired.
 * @param token
 * @returns True if the token is expired, false otherwise.
 */
function isExpired(token: PossibleString): boolean {
    if (!token) {
        return true
    }
    try {
        const decodedToken: string = atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
        const payload: any = JSON.parse(decodedToken)
        const exponent: number = (payload?.exp ?? 0) * 1000
        const skew: number = 5000 // 5s skew
        return Date.now() + skew >= exponent
    } catch {
        return true
    }
}

function absoluteUrl(request: HttpRequest<any>): string {
    return resolveAbsoluteHttpUrl(request.urlWithParams)
}

function withDpopHeaders(
    request: HttpRequest<any>,
    dpopProofService: DpopProofService,
    token?: string,
): Observable<HttpRequest<any>> {
    const url: string = absoluteUrl(request)
    return from(dpopProofService.buildProof(request.method, url, token)).pipe(
        switchMap((proof: string): Observable<HttpRequest<any>> => {
            const headers: { [name: string]: string } = {
                DPoP: proof,
            }
            if (token) {
                headers['Authorization'] = `DPoP ${token}`
            }
            const cloned: HttpRequest<any> = request.clone({
                setHeaders: headers,
            })
            return of(cloned)
        }),
    )
}

/**
 * Angular HttpInterceptor implementation that:
 * - Adds DPoP headers and Authorization: DPoP <token> to protected requests
 * - Sends withCredentials only for refresh/logout endpoints
 * - Performs proactive refresh near expiry and reactive refresh on 401 (single-flight)
 * @param request
 * @param next
 */
export const authInterceptor: HttpInterceptorFn = (request, next): Observable<any> => {
    const tokenStore: TokenStorageService = inject(TokenStorageService)
    const dpopProofService: DpopProofService = inject(DpopProofService)
    const tokenRefreshService: TokenRefreshService = inject(TokenRefreshService)

    const isAuthLogin: boolean = /\/auth\/login$/.test(request.url)
    const isAuthRefresh: boolean = /\/auth\/refresh$/.test(request.url)
    const isAuthLogout: boolean = /\/auth\/logout$/.test(request.url)
    const isAuthStatus: boolean = /\/auth\/logged-in$/.test(request.url)

    // Ensure refresh/logout send credentials for HttpOnly cookie
    if (isAuthRefresh || isAuthLogout) {
        request = request.clone({
            withCredentials: true,
        })
    }
    // Skip adding Authorization for login endpoint
    if (isAuthLogin) {
        return next(request)
    }

    // For status check, attach Bearer token (no DPoP) to allow lightweight auth
    if (isAuthStatus) {
        const token: PossibleToken = tokenStore.getToken()
        if (token) {
            const cloned: HttpRequest<unknown> = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`,
                },
            })
            return next(cloned)
        }
        return next(request)
    }

    const token: PossibleToken = tokenStore.getToken()
    const proceed = (currentToken: PossibleToken): Observable<any> => {
        // Always attach DPoP proof; include Authorization only when a token exists
        return withDpopHeaders(request, dpopProofService, currentToken || undefined).pipe(
            switchMap(
                (requestWithHeaders): Observable<HttpEvent<unknown>> => next(requestWithHeaders),
            ),
        )
    }

    // Proactive refresh
    const shouldRefreshToken: boolean = token !== null && isExpired(token) && !isAuthRefresh
    if (shouldRefreshToken) {
        if (!refreshInFlight$) {
            refreshInFlight$ = tokenRefreshService.refreshToken().pipe(
                shareReplay(1),
                finalize((): null => {
                    return (refreshInFlight$ = null)
                }),
            )
        }
        return refreshInFlight$.pipe(switchMap((newToken): Observable<any> => proceed(newToken)))
    }

    return proceed(token).pipe(
        catchError((error): Observable<HttpEvent<unknown>> => {
            // Reactive refresh on 401 (once) except for logout endpoint
            const isTokenRefreshNeeded: boolean =
                error?.status === 401 && !isAuthRefresh && !isAuthLogout
            if (isTokenRefreshNeeded) {
                if (!refreshInFlight$) {
                    refreshInFlight$ = tokenRefreshService.refreshToken().pipe(
                        shareReplay(1),
                        finalize((): null => {
                            return (refreshInFlight$ = null)
                        }),
                    )
                }
                return refreshInFlight$.pipe(
                    switchMap(
                        (newToken: string): Observable<HttpRequest<any>> =>
                            withDpopHeaders(request, dpopProofService, newToken),
                    ),
                    switchMap((cloned: HttpRequest<any>): Observable<HttpEvent<unknown>> => next(cloned)),
                )
            }
            throw error
        }),
    )
}
