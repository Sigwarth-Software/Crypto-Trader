package org.cryptotrader.data.library.services.entity

import org.cryptotrader.data.library.entity.prediction.PricePrediction
import org.cryptotrader.data.library.repository.PricePredictionRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PricePredictionEntityService @Autowired constructor(
    repository: PricePredictionRepository
) : BaseEntityService<PricePrediction, Long, PricePredictionRepository>(repository) {

}