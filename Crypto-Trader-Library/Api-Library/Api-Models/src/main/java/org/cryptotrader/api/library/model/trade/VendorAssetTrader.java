package org.cryptotrader.api.library.model.trade;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;

@Getter
@Setter
@Slf4j
public class VendorAssetTrader extends AssetTrader {
    private double currencyPrice;
    private double vendorPrice;
    private double fee;

    public VendorAssetTrader(final PortfolioAsset asset) {
        super(asset);
    }

    @Override
    public boolean trade() {
        final double currentPrice = this.asset.getCurrency().getUpdatedValue();
        final double vendorPrice = this.asset.getVendor().getAdjustedPrice(currentPrice);
        this.currencyPrice = currentPrice;
        this.vendorPrice = vendorPrice;
        this.fee = this.vendorPrice - this.currencyPrice;
        return this.trade(vendorPrice);
    }

    @Override
    public boolean trade(final double vendorPrice) {
        final double targetPrice = this.asset.getTargetPrice();
        if (vendorPrice > targetPrice) {
            if (this.asset.canSell()) {
                this.asset.setTargetPrice(vendorPrice);
                this.sell(vendorPrice);
                return true;
            }
        } else if (vendorPrice < targetPrice) {
            if (this.asset.canBuy()) {
                this.asset.setTargetPrice(vendorPrice);
                this.buy(vendorPrice);
                return true;
            }
        }
        return false;
    }

    @Override
    public void sell(final double currentPrice) {
        final double valueInDollars = this.asset.getShares() * this.currencyPrice;
        final double walletDollars = this.asset.getAssetWalletDollars() + valueInDollars;
        this.asset.setAssetWalletDollars(walletDollars);
        this.asset.updateValues();
        log.info("[{}] Selling {} shares of {} for {} dollars. Vendor, {}, had a fee of {} dollars.",
            this.getContext().toString().toUpperCase(),
            this.asset.getShares(),
            this.asset.getCurrency().getName(),
            walletDollars,
            this.asset.getVendor().getName(),
            this.fee);
        this.asset.setShares(0);
        this.asset.updateValues();
    }

    @Override
    public void buy(final double currentPrice) {
        final double shares = this.asset.getAssetWalletDollars() / this.currencyPrice;
        this.asset.setShares(shares);
        this.asset.updateValues();
        log.info("[{}] Buying {} shares of {} for {} dollars. Vendor, {}, had a fee of {} dollars.",
            this.getContext().toString().toUpperCase(),
            shares,
            this.asset.getCurrency().getName(),
            this.asset.getAssetWalletDollars(),
            this.asset.getVendor().getName(),
            this.fee);
        this.asset.setAssetWalletDollars(0);
        this.asset.updateValues();
    }
}
