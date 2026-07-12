import { Injectable } from '@angular/core'

import { defaultCurrencyIcon, ImageAsset } from '@assets/image.assets'
import { DisplayCurrency } from '@models/currency/types'

/**
 * Attempts to resolve the image asset for a given currency, if available.
 */
@Injectable({
    providedIn: 'root',
})
export class CurrencyImageService {
    constructor() {}

    /**
     * Attempts to resolve the image asset for a given currency, if available.
     * @param currency The currency for which to resolve the image asset.
     *
     * @returns {Promise<ImageAsset>} A promise that resolves to the image
     * asset for the given currency, or a default image asset if the currency
     * image is not available.
     */
    async resolveImageAsset(currency: DisplayCurrency | string): Promise<ImageAsset> {
        let src: string
        let alt: string
        if (typeof currency === 'string') {
            src = `/assets/cryptofont/${currency.toLowerCase()}.svg`
            alt = `${currency} logo`
        } else {
            src = currency.logoUrl
            alt = `${currency.currencyName} logo`
        }

        let imageAsset: ImageAsset = {
            src: src,
            alt: alt,
        }
        const imageLoads: boolean = await this.imageLoads(imageAsset.src)
        if (!imageLoads) {
            imageAsset = defaultCurrencyIcon
        }
        return imageAsset
    }

    /**
     * If the image loads successfully, resolves to true. If the image fails
     * to load, resolves to false.
     *
     * @param src The path to the image asset.
     * @returns {Promise<boolean>} A promise that resolves to true if the
     * image loads successfully, or false if it fails to load.
     */
    async imageLoads(src: string): Promise<boolean> {
        return new Promise<boolean>((resolve): void => {
            const image = new Image()
            image.onload = (): void => {
                resolve(true)
            }
            image.onerror = (): void => {
                resolve(false)
            }
            image.src = src
        })
    }
}
