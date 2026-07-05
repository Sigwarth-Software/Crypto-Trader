package org.cryptotrader.api.library.services.entity.portfolio

import org.cryptotrader.api.library.entity.portfolio.PortfolioAssetHistory
import org.cryptotrader.api.library.repository.PortfolioAssetHistoryRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PortfolioAssetHistoryEntityService @Autowired constructor(
    repository: PortfolioAssetHistoryRepository
) : BaseEntityService<PortfolioAssetHistory, Long, PortfolioAssetHistoryRepository>(repository) {

}