import { type Portfolio, type PortfolioAsset } from '@models/portfolio/types';

export const defaultPortfolio: Portfolio = {
    assets: [],
    dollarBalance: 0,
    lastTraded: '',
    shareBalance: 0,
    totalWorth: 0,
};

export const defaultPortfolioAsset: PortfolioAsset = {
    assetWalletDollars: 0,
    currencyCode: '',
    currencyName: '',
    id: 0,
    lastTraded: '',
    shares: 0,
    sharesValueInDollars: 0,
    targetPrice: 0,
    totalValueInDollars: 0,
    vendorName: '',
};
