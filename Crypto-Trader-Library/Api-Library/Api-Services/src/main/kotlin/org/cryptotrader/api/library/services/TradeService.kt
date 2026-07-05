package org.cryptotrader.api.library.services

import org.cryptotrader.api.library.communication.request.TradeRequest
import org.cryptotrader.data.library.entity.currency.Currency
import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.entity.portfolio.PortfolioAsset
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.entity.vendor.SupportedVendors
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioAssetEntityService
import org.cryptotrader.api.library.services.entity.portfolio.PortfolioEntityService
import org.cryptotrader.data.library.services.CurrencyService
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class TradeService(
    private val authContextService: AuthContextService,
    private val portfolioService: PortfolioService,
    private val currencyService: CurrencyService,
    private val productUserService: ProductUserService,
    private val portfolioEntityService: PortfolioEntityService,
    private val portfolioAssetEntityService: PortfolioAssetEntityService,
) {
    @Transactional
    open fun checkout(tradeRequest: TradeRequest): Boolean {
        val isLoggedIn: Boolean = this.authContextService.isUserAuthenticated()
        if (!isLoggedIn) {
            return false
        }
        val authenticatedUser: ProductUser = this.authContextService.getAuthenticatedProductUser() ?: return false
        val productUser: ProductUser = this.productUserService.getUserById(authenticatedUser.id) ?: return false

        return if (this.isSharesCheckout(tradeRequest)) {
            this.sharesCheckout(tradeRequest, productUser)
        } else {
            this.dollarsCheckout(tradeRequest, productUser)
        }
    }

    private fun isSharesCheckout(tradeRequest: TradeRequest): Boolean {
        return tradeRequest.numShares > 0
    }

    private fun isDollarsCheckout(tradeRequest: TradeRequest): Boolean {
        return tradeRequest.numDollars > 0
    }

    private fun sharesCheckout(tradeRequest: TradeRequest, productUser: ProductUser): Boolean {
        return this.sharesCheckout(tradeRequest, productUser, tradeRequest.numShares)
    }

    private fun sharesCheckout(tradeRequest: TradeRequest, productUser: ProductUser, numShares: Double): Boolean {
        val currencyToBuy: Currency = this.currencyService.getCurrencyByCurrencyCode(tradeRequest.currencyCode) ?: return false
        val userPortfolio: Portfolio = productUser.portfolio ?: Portfolio(productUser)

        Hibernate.initialize(userPortfolio.assets)

        val portfolioAsset: PortfolioAsset = PortfolioAsset.builder()
                                                           .portfolio(userPortfolio)
                                                           .currency(currencyToBuy)
                                                           .shares(numShares)
                                                           .build()
        portfolioAsset.updateValues()
        userPortfolio.addAsset(portfolioAsset)
        portfolioAsset.vendor = SupportedVendors.from(tradeRequest.vendor)
        this.portfolioAssetEntityService.save(portfolioAsset)
        userPortfolio.updateValues()
        this.portfolioEntityService.save(userPortfolio)
        return true
    }

    private fun dollarsCheckout(tradeRequest: TradeRequest, productUser: ProductUser): Boolean {
        val currencyToBuy: Currency = this.currencyService.getCurrencyByCurrencyCode(tradeRequest.currencyCode) ?: return false
        val numShares: Double = tradeRequest.numDollars / currencyToBuy.value
        return this.sharesCheckout(tradeRequest, productUser, numShares)
    }
}