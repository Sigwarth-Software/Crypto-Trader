package org.cryptotrader.data.library.services.entity

import org.cryptotrader.data.library.entity.currency.CurrencyHistory
import org.cryptotrader.data.library.repository.CurrencyHistoryRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CurrencyHistoryEntityService @Autowired constructor(
    repository: CurrencyHistoryRepository
) : BaseEntityService<CurrencyHistory, Long, CurrencyHistoryRepository>(repository) {

}