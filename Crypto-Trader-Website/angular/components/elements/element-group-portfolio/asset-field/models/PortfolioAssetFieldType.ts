export enum PortfolioAssetFieldType {
    CURRENCY_NAME = 'Name',
    TARGET_PRICE = 'Trade Price',
    LAST_UPDATED = 'Last Traded',
    VENDOR = 'Vendor',
    SHARES = 'Shares',
    TOTAL_VALUE = 'Total Value',
}
export namespace PortfolioAssetFieldType {
    export function values(): PortfolioAssetFieldType[] {
        return [
            PortfolioAssetFieldType.CURRENCY_NAME,
            PortfolioAssetFieldType.SHARES,
            PortfolioAssetFieldType.TOTAL_VALUE,
            PortfolioAssetFieldType.TARGET_PRICE,
            PortfolioAssetFieldType.VENDOR,
            PortfolioAssetFieldType.LAST_UPDATED,
        ];
    }
}
