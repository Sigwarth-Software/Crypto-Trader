package org.cryptotrader.api.library.services.entity.portfolio

import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset
import org.cryptotrader.api.library.repository.PortfolioAssetRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PortfolioAssetEntityService @Autowired constructor(
    repository: PortfolioAssetRepository
) : BaseEntityService<PortfolioAsset, Long, PortfolioAssetRepository>(repository) {

}