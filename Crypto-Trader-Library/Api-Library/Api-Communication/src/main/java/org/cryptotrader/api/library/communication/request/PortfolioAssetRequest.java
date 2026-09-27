package org.cryptotrader.api.library.communication.request;
//=================================-Imports-==================================
import lombok.Data;

@Data
public class PortfolioAssetRequest {
    //============================-Variables-=================================
    String currencyName;
    double shares;
    double walletDollars;
    //===========================-Constructors-===============================
    public PortfolioAssetRequest(final String currencyName, final double shares, final double walletDollars) {
        this.currencyName = currencyName;
        this.shares = shares;
        this.walletDollars = walletDollars;
    }
}
