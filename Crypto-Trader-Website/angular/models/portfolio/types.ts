export type Portfolio = {
    dollarBalance: number;
    shareBalance: number;
    totalWorth: number;
    lastUpdated: string;
    assets: PortfolioAsset[];
};

export type PortfolioAsset = {
    id: number;
    currencyName: string;
    currencyCode: string;
    shares: number;
    sharesValueInDollars: number;
    assetWalletDollars: number;
    totalValueInDollars: number;
    targetPrice: number;
    lastUpdated: string;
    vendorName: string;
};

export type PortfolioHistory = {
    id: number
    dollarBalance: number
    shareBalance: number
    totalWorth: number
    valueChange: number
    tradeOccurred: boolean
    lastUpdated: string
}

export type RangedPortfolioHistoryRequest = {
    startDate: string
    endDate: string
}