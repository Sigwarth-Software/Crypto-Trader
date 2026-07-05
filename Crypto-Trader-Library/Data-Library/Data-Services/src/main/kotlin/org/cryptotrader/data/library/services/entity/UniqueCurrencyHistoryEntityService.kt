package org.cryptotrader.data.library.services.entity

import org.cryptotrader.data.library.entity.currency.UniqueCurrencyHistory
import org.cryptotrader.data.library.repository.UniqueCurrencyHistoryRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UniqueCurrencyHistoryEntityService @Autowired constructor(
    repository: UniqueCurrencyHistoryRepository
) : BaseEntityService<UniqueCurrencyHistory, Long, UniqueCurrencyHistoryRepository>(repository) {

}