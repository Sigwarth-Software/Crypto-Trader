package org.cryptotrader.data.library.services.entity

import org.cryptotrader.data.library.entity.news.NewsSentiment
import org.cryptotrader.data.library.repository.NewsSentimentRepository
import org.cryptotrader.universal.library.services.BaseEntityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NewsSentimentEntityService @Autowired constructor(
    repository: NewsSentimentRepository
) : BaseEntityService<NewsSentiment, Long, NewsSentimentRepository>(repository){
}