import { Injectable } from '@angular/core'
import { environment } from '@environments/environment'

/**
 * Stores and retrieves the DPoP CryptoKeyPair using IndexedDB when enabled.
 */
@Injectable({
    providedIn: 'root',
})
export class DpopKeyStoreService {
    private readonly dbName: string = 'crypto-trader-auth'
    private readonly storeName: string = 'dpop'
    private readonly keyId: string = 'pair'

    /**
     * Indicates whether the DPoP key store is enabled.
     * @returns {boolean} True if the DPoP key store is enabled, false otherwise.
     */
    public get enabled(): boolean {
        return environment.persistDpopKey
    }

    /**
     * Saves the DPoP key pair to the browser database.
     * @param pair The DPoP key pair to save.
     */
    public async save(pair: CryptoKeyPair): Promise<void> {
        if (!this.enabled) {
            return
        }
        try {
            const db: IDBDatabase = await this.openDb()
            const tx: IDBTransaction = db.transaction(this.storeName, 'readwrite')
            const store: IDBObjectStore = tx.objectStore(this.storeName)
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
        if (!this.enabled) {
            return null
        }
        try {
            const db: IDBDatabase = await this.openDb()
            const tx: IDBTransaction = db.transaction(this.storeName, 'readonly')
            const store: IDBObjectStore = tx.objectStore(this.storeName)
            const result: any = await requestToPromise(store.get(this.keyId))
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
        if (!this.enabled) {
            return
        }
        try {
            const db: IDBDatabase = await this.openDb()
            const tx: IDBTransaction = db.transaction(this.storeName, 'readwrite')
            const store: IDBObjectStore = tx.objectStore(this.storeName)
            await requestToPromise(store.delete(this.keyId))
            db.close?.()
        } catch {
            // ignore
        }
    }

    private openDb(): Promise<IDBDatabase> {
        return new Promise((resolve, reject): void => {
            const req: IDBOpenDBRequest = indexedDB.open(this.dbName, 1)
            req.onupgradeneeded = (): void => {
                const db: IDBDatabase = req.result

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
