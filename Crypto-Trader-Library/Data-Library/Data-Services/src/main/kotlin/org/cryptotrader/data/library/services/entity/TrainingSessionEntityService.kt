package org.cryptotrader.data.library.services.entity

import org.cryptotrader.data.library.entity.training.TrainingSession
import org.cryptotrader.data.library.repository.TrainingSessionRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TrainingSessionEntityService @Autowired constructor(
    repository: TrainingSessionRepository
) : BaseEntityService<TrainingSession, Long, TrainingSessionRepository>(repository) {

}