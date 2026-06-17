import { Injectable } from '@angular/core'
import { BehaviorSubject, Observable } from 'rxjs'

import { PersistMethod, PossibleToken } from '@models/auth/types'

/**
 * In-memory-only access token storage. No localStorage/sessionStorage usage.
 * Provides both imperative getter and reactive observable.
 */
@Injectable({
    providedIn: 'root',
})
export class TokenStorageService {
    private readonly token$: BehaviorSubject<PossibleToken> = new BehaviorSubject<PossibleToken>(null)

    /**
     * Gets the token stored in memory.
     * @returns The token stored in memory.
     */
    public getToken(): PossibleToken {
        return this.token$.value
    }

    /**
     * The token stored in memory as an observable.
     * @returns The token as an observable.
     */
    public observe(): Observable<PossibleToken> {
        return this.token$.asObservable()
    }

    /**
     * Sets the token stored in memory.
     *
     * @param token
     * @param _persist
     */
    public setToken(token: PossibleToken, _persist: PersistMethod = 'session'): void {
        // persist parameter kept for API compatibility but ignored to enforce in-memory policy
        this.token$.next(token)
    }

    /**
     * Clears the token stored in memory.
     */
    public clear(): void {
        this.token$.next(null)
    }
}
