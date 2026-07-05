package org.cryptotrader.api.library.services.entity.portfolio

import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.repository.PortfolioRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PortfolioEntityService @Autowired constructor(
    repository: PortfolioRepository
) : BaseEntityService<Portfolio, Long, PortfolioRepository>(repository) {

}