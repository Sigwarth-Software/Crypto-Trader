package org.cryptotrader.simulator.library.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.cryptotrader.api.library.entity.portfolio.Portfolio;

@Getter
@Setter
@Entity
@Table(name = "portfolio_simulations")
public class PortfolioSimulation extends Simulation<Portfolio> {
    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false, updatable = false)
    private Portfolio portfolio;
}
