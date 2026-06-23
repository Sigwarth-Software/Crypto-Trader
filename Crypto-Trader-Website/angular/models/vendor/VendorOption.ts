import {
    binanceBrandLogo,
    coinbaseBrandLogo,
    paperIcon,
    type ImageAsset,
} from '@assets/image.assets';

export enum VendorOption {
    PAPER_MODE = 'Paper Mode',
    COINBASE = 'Coinbase',
    BINANCE = 'Binance',
}
export namespace VendorOption {
    export function from(value: string): VendorOption {
        for (const option of values()) {
            if (option.toString() === value) {
                return option;
            }
        }
        throw new Error(`No VendorOption found for value: ${value}`);
    }

    export function getImageAsset(option: VendorOption): ImageAsset {
        switch (option) {
            case VendorOption.PAPER_MODE:
                return paperIcon;
            case VendorOption.COINBASE:
                return coinbaseBrandLogo;
            case VendorOption.BINANCE:
                return binanceBrandLogo;
        }
    }
    export function values(): VendorOption[] {
        return [
            VendorOption.PAPER_MODE,
            VendorOption.COINBASE,
            VendorOption.BINANCE,
        ];
    }
}
