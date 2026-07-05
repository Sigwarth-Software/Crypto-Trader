package org.cryptotrader.api.library.repository;
//=================================-Imports-==================================

import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    //=============================-Methods-==================================

    //---------------------Find-Portfolio-By-User-Id--------------------------
    @EntityGraph(attributePaths = {"assets", "assets.currency"})
    Portfolio findPortfolioByUserId(Long userId);
}
