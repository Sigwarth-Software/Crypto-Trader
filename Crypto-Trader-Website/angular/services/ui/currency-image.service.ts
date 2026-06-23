import { Injectable } from '@angular/core';

import { defaultCurrencyIcon, ImageAsset } from '@assets/image.assets';
import { DisplayCurrency } from '@models/currency/types';

@Injectable({
    providedIn: 'root',
})
export class CurrencyImageService {
    constructor() {}

    async resolveImageAsset(
        currency: DisplayCurrency | string,
    ): Promise<ImageAsset> {
        let src: string;
        let alt: string;
        if (typeof currency === 'string') {
            src = `/assets/cryptofont/${currency.toLowerCase()}.svg`;
            alt = currency + ' logo';
        } else {
            src = currency.logoUrl;
            alt = currency.currencyName + ' logo';
        }

        let imageAsset: ImageAsset = {
            src: src,
            alt: alt,
        };
        const imageLoads: boolean = await this.imageLoads(imageAsset.src);
        if (!imageLoads) {
            imageAsset = defaultCurrencyIcon;
        }
        return imageAsset;
    }

    async imageLoads(src: string): Promise<boolean> {
        return new Promise<boolean>((resolve) => {
            const image = new Image();
            image.onload = () => {
                resolve(true);
            };
            image.onerror = () => {
                resolve(false);
            };
            image.src = src;
        });
    }
}
