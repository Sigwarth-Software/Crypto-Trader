package org.cryptotrader.api.library.model.trade;
//=================================-Imports-==================================
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;

@Getter
@Setter
@Slf4j
public class AssetTrader extends TradeEngine {
    //============================-Variables-=================================
    protected PortfolioAsset asset;
    protected double minProfitPercent;
    private static final double DEFAULT_MIN_PROFIT_PERCENT = 0.01;
    //===========================-Constructors-===============================
    public AssetTrader(final PortfolioAsset asset) {
        this(asset, DEFAULT_MIN_PROFIT_PERCENT);
    }

    public AssetTrader(final PortfolioAsset asset, final TradeContext context) {
        super(context);
        this.asset = asset;
        this.minProfitPercent = DEFAULT_MIN_PROFIT_PERCENT;
    }

    public AssetTrader(final PortfolioAsset asset, final double minProfitPercent) {
        this.asset = asset;
        this.minProfitPercent = minProfitPercent;
    }

    public AssetTrader(final PortfolioAsset asset, final TradeContext context, final double minProfitPercent) {
        super(context);
        this.asset = asset;
        this.minProfitPercent = minProfitPercent;
    }

    //=============================-Methods-==================================

    //-------------------------------Trade------------------------------------
    @Override
    public boolean trade() {
        final double currentPrice = this.asset.getCurrency().getUpdatedValue();
        return this.trade(currentPrice);
    }
    //-------------------------------Trade------------------------------------
    @Override
    public boolean trade(final double currentPrice) {
        final double targetPrice = this.asset.getTargetPrice();
        final double priceChangePercent = Math.abs(currentPrice - targetPrice) / targetPrice;

        if (priceChangePercent < this.minProfitPercent) {
            return false;
        }

        if (currentPrice > targetPrice) {
            if (this.asset.canSell()) {
                this.asset.setTargetPrice(currentPrice);
                this.sell(currentPrice);
                return true;
            }
        } else if (currentPrice < targetPrice) {
            if (this.asset.canBuy()) {
                this.asset.setTargetPrice(currentPrice);
                this.buy(currentPrice);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canTrade() {
        final double currentPrice = this.asset.getCurrency().getUpdatedValue();
        return this.canTrade(currentPrice);
    }

    @Override
    public boolean canTrade(final double currentPrice) {
        final double targetPrice = this.asset.getTargetPrice();
        final double priceChangePercent = Math.abs(currentPrice - targetPrice) / targetPrice;

        if (priceChangePercent < this.minProfitPercent) {
            return false;
        }

        if (currentPrice > targetPrice) {
            if (this.asset.canSell()) {
                this.asset.setTargetPrice(currentPrice);
                return true;
            }
        } else if (currentPrice < targetPrice) {
            if (this.asset.canBuy()) {
                this.asset.setTargetPrice(currentPrice);
                return true;
            }
        }
        return false;
    }

    //--------------------------------Sell------------------------------------
    @Override
    public void sell(final double currentPrice) {
        final double valueInDollars = this.asset.getShares() * currentPrice;
        final double walletDollars = this.asset.getAssetWalletDollars() + valueInDollars;
        this.asset.setAssetWalletDollars(walletDollars);
        this.asset.updateValues();
        log.info("[{}] Selling {} shares of {} for {} dollars.",
            this.getContext().toString().toUpperCase(),
            this.asset.getShares(),
            this.asset.getCurrency().getName(),
            walletDollars);
        this.asset.setShares(0);
        this.asset.updateValues();
    }
    //---------------------------------Buy------------------------------------
    @Override
    public void buy(final double currentPrice) {
        final double shares = this.asset.getAssetWalletDollars() / currentPrice;
        this.asset.setShares(shares);
        this.asset.updateValues();
        log.info("[{}] Buying {} shares of {} for {} dollars.",
            this.getContext().toString().toUpperCase(),
            shares,
            this.asset.getCurrency().getName(),
            this.asset.getAssetWalletDollars());
        this.asset.setAssetWalletDollars(0);
        this.asset.updateValues();
    }
}
