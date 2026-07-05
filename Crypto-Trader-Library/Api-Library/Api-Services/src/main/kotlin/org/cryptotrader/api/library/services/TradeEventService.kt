package org.cryptotrader.api.library.services

import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset
import org.cryptotrader.api.library.entity.portfolio.PortfolioAssetHistory
import org.cryptotrader.api.library.entity.trade.TradeEvent
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.repository.TradeEventRepository
import org.cryptotrader.api.library.services.entity.trade.TradeEventEntityService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class TradeEventService(
    val tradeEventRepository: TradeEventRepository,
    val tradeEventEntityService: TradeEventEntityService,
    val portfolioService: PortfolioService
) {
    fun saveTradeEvent(tradeEvent: TradeEvent): TradeEvent {
        return this.tradeEventEntityService.save(tradeEvent)
    }

    fun getAssetByTradeEvent(tradeEvent: TradeEvent): PortfolioAsset {
        val assetHistory: PortfolioAssetHistory = tradeEvent.assetHistory
        return assetHistory.portfolioAsset
    }

    fun getAllByProductUser(productUser: ProductUser): List<TradeEvent> {
        return this.tradeEventRepository.findAllByPortfolioId(productUser.portfolio.id)
    }

    fun getSelectionByProductUser(productUser: ProductUser, offset: Int, size: Int): List<TradeEvent> {
        val pageable: Pageable = Pageable.ofSize(size).withPage(offset / size)
        return this.tradeEventRepository.findAllByPortfolioIdOrderByTradeTimeDesc(productUser.portfolio.id, pageable)
    }

    fun userHasTrades(productUser: ProductUser): Boolean {
        return this.tradeEventRepository.existsTradeEventByPortfolioId(productUser.portfolio.id)
    }
}