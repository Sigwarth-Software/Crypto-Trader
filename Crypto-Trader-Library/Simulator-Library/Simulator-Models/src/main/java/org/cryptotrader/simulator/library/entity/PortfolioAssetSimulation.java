package org.cryptotrader.simulator.library.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset;

@Entity
@Table(name = "portfolio_asset_simulations")
public class PortfolioAssetSimulation extends Simulation<PortfolioAsset> {

}
