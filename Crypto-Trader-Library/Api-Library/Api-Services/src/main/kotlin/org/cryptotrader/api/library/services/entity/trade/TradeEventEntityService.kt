package org.cryptotrader.api.library.services.entity.trade

import org.cryptotrader.api.library.entity.trade.TradeEvent
import org.cryptotrader.api.library.repository.TradeEventRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TradeEventEntityService @Autowired constructor(
    repository: TradeEventRepository
) : BaseEntityService<TradeEvent, Long, TradeEventRepository>(repository) {

}