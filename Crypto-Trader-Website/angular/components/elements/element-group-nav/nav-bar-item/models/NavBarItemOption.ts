import { type ElementLink } from '@theoliverlear/angular-suite'
import {
    currenciesElementLink,
    portfolioElementLink,
    simulatorElementLink,
    statisticsElementLink,
    tradeElementLink,
    traderElementLink,
} from '@assets/elementLinkAssets'
import {
    circleCheckmarkIcon,
    coinIcon,
    exchangeArrowsIcon,
    stockScaleIcon,
    walletIcon,
    type ImageAsset,
} from '@assets/imageAssets';

/** The options for a navigation bar item.
 *
 */
export enum NavBarItemOption {
    Currencies = 'Currencies',
    Portfolio = 'Portfolio',
    Trader = 'Trader',
    Trade = 'Trade',
    Statistics = 'Statistics',
    Simulator = 'Simulator',
}
export namespace NavBarItemOption {
    /** Returns the image asset for the given option.
     *
     * @param option
     * @returns The image asset for the given option.
     */
    export function getImageAsset(option: NavBarItemOption): ImageAsset {
        switch (option) {
            case NavBarItemOption.Currencies:
            case NavBarItemOption.Portfolio:
                return walletIcon
            case NavBarItemOption.Trader:
                return circleCheckmarkIcon
            case NavBarItemOption.Trade:
                return exchangeArrowsIcon
            case NavBarItemOption.Statistics:
                return stockScaleIcon
            case NavBarItemOption.Simulator:
                return whitePotionIcon
            default:
        }
    }

    /** Returns the element link for the given option.
     *
     * @param option
     * @returns The element link for the given option.
     */
    export function getElementLink(option: NavBarItemOption): ElementLink {
        switch (option) {
            case NavBarItemOption.Trade:
                return tradeElementLink
            case NavBarItemOption.Statistics:
                return statisticsElementLink
            case NavBarItemOption.Simulator:
                return simulatorElementLink
            default:
                throw new Error(`Invalid option: ${option}`)
        }
    }

    /** Returns the values of the enum.
     *
     * @returns The values of the enum.
     */
    export function values(): NavBarItemOption[] {
        return [
            NavBarItemOption.CURRENCIES,
            NavBarItemOption.PORTFOLIO,
            NavBarItemOption.Currencies,
            NavBarItemOption.Portfolio,
            NavBarItemOption.Trader,
            NavBarItemOption.Trade,
            NavBarItemOption.Statistics,
            NavBarItemOption.Simulator,
        ]
    }
}
