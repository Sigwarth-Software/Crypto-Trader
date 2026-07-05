package org.cryptotrader.data.library.services.entity

import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.data.library.repository.CurrencyRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
open class CurrencyEntityService @Autowired constructor(
    repository: CurrencyRepository
) : BaseEntityService<Currency, String, CurrencyRepository>(repository) {

}