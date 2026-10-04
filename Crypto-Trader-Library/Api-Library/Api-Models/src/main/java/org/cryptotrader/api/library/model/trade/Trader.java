package org.cryptotrader.api.library.model.trade;
//=================================-Imports-==================================
import lombok.Getter;
import lombok.Setter;
import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;
import org.cryptotrader.api.library.entity.vendor.SupportedVendors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Trader {
    //============================-Variables-=================================
    // TODO: Allow for trades based on target prices and a strategy.
    private Portfolio portfolio;
    private List<TradingEngine> assetTraders;
    //===========================-Constructors-===============================
    public Trader(final Portfolio portfolio) {
        this.portfolio = portfolio;
        this.assetTraders = new ArrayList<>();
        this.initializeTraders();
    }
    //============================-Methods-===================================

    //----------------------Initialize-Asset-Traders--------------------------
    public void initializeTraders() {
        for (final PortfolioAsset asset : this.portfolio.getAssets()) {
            if (asset.getVendor().equals(SupportedVendors.PAPER_MODE)) {
                this.assetTraders.add(new AssetTrader(asset));
            } else {
                this.assetTraders.add(new VendorAssetTrader(asset));
            }
        }
    }
    //--------------------------Trade-All-Assets------------------------------
    public void tradeAllAssets() {
        for (final TradingEngine assetTrader : this.assetTraders) {
            assetTrader.trade();
        }
    }
}
