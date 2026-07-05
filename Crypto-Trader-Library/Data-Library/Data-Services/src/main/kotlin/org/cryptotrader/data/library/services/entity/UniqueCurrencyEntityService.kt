package org.cryptotrader.data.library.services.entity

import org.cryptotrader.data.library.entity.currency.UniqueCurrency
import org.cryptotrader.data.library.repository.UniqueCurrencyRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UniqueCurrencyEntityService @Autowired constructor(
    repository: UniqueCurrencyRepository
) : BaseEntityService<UniqueCurrency, String, UniqueCurrencyRepository>(repository) {

}