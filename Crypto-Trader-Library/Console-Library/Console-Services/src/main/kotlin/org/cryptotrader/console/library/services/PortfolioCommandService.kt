package org.cryptotrader.console.library.services

import org.cryptotrader.api.library.communication.response.PortfolioResponse
import org.cryptotrader.api.library.entity.portfolio.Portfolio
import org.cryptotrader.api.library.entity.user.ProductUser
import org.cryptotrader.api.library.services.AuthContextService
import org.cryptotrader.api.library.services.PortfolioService
import org.cryptotrader.console.library.communication.response.ConsoleCommandResponse
import org.cryptotrader.console.library.infrastructure.annotation.CommandHelp
import org.cryptotrader.console.library.model.command.PortfolioCommand
import org.cryptotrader.console.library.services.models.BaseConsoleCommandRunner
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@CommandHelp(
    command = "portfolio",
    description = "Portfolio commands"
)
@Service
open class PortfolioCommandService @Autowired constructor(
    private val portfolioService: PortfolioService,
    private val authContextService: AuthContextService
) : BaseConsoleCommandRunner<PortfolioCommand>(PortfolioCommand.TOP_LEVEL) {

    @Transactional(readOnly = true)
    override fun executeResolvedCommand(command: PortfolioCommand, commandInput: String): ConsoleCommandResponse {
        return when (command) {
            PortfolioCommand.TOP_LEVEL, PortfolioCommand.SHOW -> this.executeShowCommand()
        }
    }

    // TODO: Should be moved to portfolio service class.
    open fun getInitializedPortfolio(userId: Long): Portfolio {
        val portfolio: Portfolio =
            this.portfolioService.getPortfolioByUserId(userId)
                ?: return Portfolio()
        Hibernate.initialize(portfolio.assets)
        return portfolio
    }

    @CommandHelp(
        command = "portfolio show",
        description = "Show the user's portfolio"
    )
    private fun executeShowCommand(): ConsoleCommandResponse {
        val currentUser: ProductUser = this.authContextService.getAuthenticatedProductUser() ?:
            return ConsoleCommandResponse("No authenticated user found.")
        val userPortfolio: Portfolio = this.getInitializedPortfolio(currentUser.id)
        return ConsoleCommandResponse(userPortfolio.toString(),
            PortfolioResponse(userPortfolio))
    }
}
