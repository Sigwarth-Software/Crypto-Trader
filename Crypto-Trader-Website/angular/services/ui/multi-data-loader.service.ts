import { Injectable } from '@angular/core'

/**
 * A service that manages the loading state of multiple data loaders.
 */
@Injectable({
    providedIn: 'root',
})
export class MultiDataLoaderService {
    private _dataLoaders: Record<string, boolean> = {}

    /**
     * Checks if any data loader is currently loading.
     * @returns {boolean} True if any data loader is loading, false otherwise.
     */
    public isLoading(): boolean {
        return Object.values(this._dataLoaders).some((isLoading: boolean): boolean => isLoading)
    }


    /**
     * Sets the loading state for a specific data loader.
     * @param {string} key - The key identifying the data loader.
     * @param {boolean} isLoading - The loading state to set (true for loading, false for not loading).
     */
    public setLoading(key: string, isLoading: boolean): void {
        this._dataLoaders[key] = isLoading
    }

    /**
     * Gets the loading state for a specific data loader.
     * @param {string} key - The key identifying the data loader.
     * @returns {boolean} The loading state of the data loader (true if loading, false if not loading).
     */
    public getLoading(key: string): boolean {
        return this._dataLoaders[key] || false
    }
}