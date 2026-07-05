import { Injectable } from '@angular/core'
import { environment } from '@environments/environment'

/**
 * Stores and retrieves the DPoP CryptoKeyPair using IndexedDB when enabled.
 *
 * Notes:
 * - We attempt to store CryptoKey objects directly using structured clone.
 * - If the browser refuses (older Safari, etc.), methods will resolve to null/no-op.
 * - Feature-flagged by environment.persistDpopKey (default false).
 */
@Injectable({
    providedIn: 'root',
})
export class DpopKeyStoreService {
    private readonly dbName = 'crypto-trader-auth'
    private readonly storeName = 'dpop'
    private readonly keyId = 'pair'

    // TODO: Clean up code.
    /**
     * Indicates whether the DPoP key store is enabled.
     * @returns {boolean} True if the DPoP key store is enabled, false otherwise.
     */
    public get enabled(): boolean {
        return !!environment.persistDpopKey
    }

    /**
     * Saves the DPoP key pair to the browser database.
     * @param pair The DPoP key pair to save.
     */
    public async save(pair: CryptoKeyPair): Promise<void> {
        if (!this.enabled) return
        try {
            const db = await this.openDb()
            const tx = db.transaction(this.storeName, 'readwrite')
            const store = tx.objectStore(this.storeName)
            await requestToPromise(store.put(pair, this.keyId))
            db.close?.()
        } catch {
            // Ignore persistence failures to remain resilient
        }
    }

    /**
     * Loads the key store into the browser database.
     * @returns {Promise<CryptoKeyPair | null>} The loaded key pair or null if
     * the key store is not enabled or successful.
     */
    public async load(): Promise<CryptoKeyPair | null> {
        if (!this.enabled) return null
        try {
            const db = await this.openDb()
            const tx = db.transaction(this.storeName, 'readonly')
            const store = tx.objectStore(this.storeName)
            const result = await requestToPromise(store.get(this.keyId))
            db.close?.()
            return (result as CryptoKeyPair) ?? null
        } catch {
            return null
        }
    }

    /**
     * Clears the DPoP key store.
     */
    public async clear(): Promise<void> {
        if (!this.enabled) return
        try {
            const db = await this.openDb()
            const tx = db.transaction(this.storeName, 'readwrite')
            const store = tx.objectStore(this.storeName)
            await requestToPromise(store.delete(this.keyId))
            db.close?.()
        } catch {
            // ignore
        }
    }

    private openDb(): Promise<IDBDatabase> {
        return new Promise((resolve, reject): void => {
            const req = indexedDB.open(this.dbName, 1)
            req.onupgradeneeded = (): void => {
                const db = req.result
                if (!db.objectStoreNames.contains(this.storeName)) {
                    db.createObjectStore(this.storeName)
                }
            }
            req.onsuccess = (): void => resolve(req.result)
            // eslint-disable-next-line @typescript-eslint/prefer-promise-reject-errors
            req.onerror = (): void => reject(req.error)
        })
    }
}

function requestToPromise<T = any>(request: IDBRequest): Promise<T> {
    return new Promise((resolve, reject): void => {
        request.onsuccess = (): void => resolve(request.result as T)
        // eslint-disable-next-line @typescript-eslint/prefer-promise-reject-errors
        request.onerror = (): void => reject(request.error)
    })
}
