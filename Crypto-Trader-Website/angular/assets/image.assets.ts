/** Image assets for use in Angular templates.
 *
 */
export interface ImageAsset {
    src: string
    alt: string
}

/** Returns the full path to an image asset.
 *
 * @param fileName
 * @returns The full path to the image asset.
 */
export function getImagePath(fileName: string): string {
    return imageAssetPath + fileName
}
/** Returns the full path to a icon image asset.
 *
 * @param fileName
 * @returns The full path to the icon image asset.
 */
export function getIconImagePath(fileName: string): string {
    return iconImageAssetPath + fileName
}

/**
 * Returns the full path to a module icon image asset.
 * @param fileName
 * @returns The full path to the module icon image asset.
 */
export function getModuleIconImagePath(fileName: string): string {
    return moduleIconImageAssetPath + fileName
}

/** Returns the full path to a Crypto Trader logo image asset.
 *
 * @param fileName
 * @returns The full path to the Crypto Trader logo image asset.
 */
export function getCryptoTraderLogoPath(fileName: string): string {
    return cryptoTraderLogoImageAssetPath + fileName
}
/** Returns the full path to a brand logo image asset.
 *
 * @param fileName
 * @returns The full path to the brand logo image asset.
 */
export function getBrandLogoPath(fileName: string): string {
    return brandLogoImageAssetPath + fileName
}
/** Returns the full path to a currency logo image asset.
 *
 * @param fileName
 * @returns The full path to the currency logo image asset.
 */
export function getCurrencyLogoPath(fileName: string): string {
    return currencyLogoImageAssetPath + fileName
}

const imageAssetPath: string = 'assets/images/'
const logoImageAssetPath: string = `${imageAssetPath}logos/`
const iconImageAssetPath: string = `${imageAssetPath}icons/`
const moduleIconImageAssetPath: string = `${iconImageAssetPath}modules/`
const cryptoTraderLogoImageAssetPath: string = `${logoImageAssetPath}crypto_trader/`
const brandLogoImageAssetPath: string = `${logoImageAssetPath}brand/`
const currencyLogoImageAssetPath: string = `${logoImageAssetPath}currency/`

export const defaultAvatar: ImageAsset = {
    src: getIconImagePath('default_avatar.png'),
    alt: 'Default Avatar',
}

export const transparentLogo: ImageAsset = {
    src: getCryptoTraderLogoPath('crypto_trader_logo_cropped_transparent.png'),
    alt: 'Transparent Logo',
}

export const stockIcon: ImageAsset = {
    src: getIconImagePath('stock_icon.svg'),
    alt: 'Stock Icon',
}

export const brainIcon: ImageAsset = {
    src: getIconImagePath('brain_icon.svg'),
    alt: 'Brain Icon',
}

export const aiStarIcon: ImageAsset = {
    src: getIconImagePath('ai_star_icon.svg'),
    alt: 'AI Star Icon',
}

export const defaultCurrencyIcon: ImageAsset = {
    src: getIconImagePath('default_currency_icon.png'),
    alt: 'Default Currency Icon',
}

export const upArrowIcon: ImageAsset = {
    src: getIconImagePath('up_arrow_icon.svg'),
    alt: 'Up Arrow Icon',
}

export const consoleIcon: ImageAsset = {
    src: getIconImagePath('console_icon.svg'),
    alt: 'Console Icon',
}

export const profileIcon: ImageAsset = {
    src: getIconImagePath('profile_icon.svg'),
    alt: 'Profile Icon',
}

export const exitIcon: ImageAsset = {
    src: getIconImagePath('white_exit_door_icon.svg'),
    alt: 'Exit Icon',
}

export const walletIcon: ImageAsset = {
    src: getIconImagePath('wallet_icon.svg'),
    alt: 'Wallet Icon',
}

export const coinIcon: ImageAsset = {
    src: getIconImagePath('coin_icon.svg'),
    alt: 'Coin Icon',
}

export const stockScaleIcon: ImageAsset = {
    src: getIconImagePath('stock_scale_icon.svg'),
    alt: 'Stock Scale Icon',
}

export const exchangeArrowsIcon: ImageAsset = {
    src: getIconImagePath('exchange_arrows_icon.svg'),
    alt: 'Soft Exchange Arrows Icon',
}

export const paperIcon: ImageAsset = {
    src: getIconImagePath('paper_icon.svg'),
    alt: 'Paper Icon',
}

export const starIcon: ImageAsset = {
    src: getIconImagePath('star_icon.png'),
    alt: 'Star Icon',
}

export const coinbaseBrandLogo: ImageAsset = {
    src: getBrandLogoPath('coinbase_logo.svg'),
    alt: 'Coinbase Logo',
}

export const binanceBrandLogo: ImageAsset = {
    src: getBrandLogoPath('binance_logo.png'),
    alt: 'Binance Logo',
}

export const dollarIcon: ImageAsset = {
    src: getIconImagePath('dollar_icon.svg'),
    alt: 'Dollar Icon',
}

export const circleCheckmarkIcon: ImageAsset = {
    src: getIconImagePath('circle_checkmark_icon.svg'),
    alt: 'Circle Checkmark Icon',
}

export const bookIcon: ImageAsset = {
    src: getIconImagePath('book_icon.svg'),
    alt: 'Book Icon',
}

export const electricPlugIcon: ImageAsset = {
    src: getIconImagePath('electric_plug_icon.svg'),
    alt: 'Electric Plug Icon',
}

export const adminModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('admin.svg'),
    alt: 'Admin Module Icon',
}

export const analysisModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('analysis.svg'),
    alt: 'Analysis Module Icon',
}

export const assetsModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('assets.svg'),
    alt: 'Assets Module Icon',
}

export const chatModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('chat.svg'),
    alt: 'Chat Module Icon',
}

export const consoleModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('console.svg'),
    alt: 'Console Module Icon',
}

export const contactModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('contact.svg'),
    alt: 'Contact Module Icon',
}

export const coverageModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('coverage.svg'),
    alt: 'Coverage Module Icon',
}

export const dataModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('data.svg'),
    alt: 'Data Module Icon',
}

export const docsModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('docs.svg'),
    alt: 'Docs Module Icon',
}

export const healthModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('health.svg'),
    alt: 'Health Module Icon',
}

export const engineModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('engine.svg'),
    alt: 'Engine Module Icon',
}

export const libraryModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('library.svg'),
    alt: 'Library Module Icon',
}

export const loggingModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('logging.svg'),
    alt: 'Logging Module Icon',
}

export const mobileModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('mobile.svg'),
    alt: 'Mobile Module Icon',
}

export const securityModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('security.svg'),
    alt: 'Security Module Icon',
}

export const simulatorModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('simulator.svg'),
    alt: 'Simulator Module Icon',
}

export const testingModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('testing.svg'),
    alt: 'Testing Module Icon',
}

export const transactionsModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('transactions.svg'),
    alt: 'Transactions Module Icon',
}

export const versionModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('version.svg'),
    alt: 'Version Module Icon',
}

export const websiteModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('website.svg'),
    alt: 'Website Module Icon',
}

export const apiModuleIcon: ImageAsset = {
    src: getModuleIconImagePath('api.svg'),
    alt: 'Api Module Icon',
}

export const personaBelieverIcon: ImageAsset = {
    src: getIconImagePath('hand-holding-heart.svg'),
    alt: 'Believer Persona Icon',
}

export const personaStrategistIcon: ImageAsset = {
    src: getIconImagePath('chess-knight.svg'),
    alt: 'Strategist Persona Icon',
}

export const personaEqualizerIcon: ImageAsset = {
    src: getIconImagePath('scale-balanced.svg'),
    alt: 'Equalizer Persona Icon',
}

export const personaBuilderIcon: ImageAsset = {
    src: getIconImagePath('hammer.svg'),
    alt: 'Builder Persona Icon',
}

export const personaTycoonIcon: ImageAsset = {
    src: getIconImagePath('crown.svg'),
    alt: 'Tycoon Persona Icon',
}

export const potionIcon: ImageAsset = {
    src: getIconImagePath('potion_icon.svg'),
    alt: 'Potion Icon',
}

export const whitePotionIcon: ImageAsset = {
    src: getIconImagePath('white_potion_icon.svg'),
    alt: 'White Potion Icon',
}
