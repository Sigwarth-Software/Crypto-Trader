package org.cryptotrader.api.library.services.entity.portfolio

import org.cryptotrader.api.library.entity.portfolio.PortfolioHistory
import org.cryptotrader.api.library.repository.PortfolioHistoryRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PortfolioHistoryEntityService @Autowired constructor(
    repository: PortfolioHistoryRepository
) : BaseEntityService<PortfolioHistory, Long, PortfolioHistoryRepository>(repository) {

}