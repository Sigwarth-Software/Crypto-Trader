package org.cryptotrader.api.controller

import jakarta.annotation.security.PermitAll
import org.cryptotrader.simulator.library.communication.request.DetailedPortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.request.PortfolioSimulationRequest
import org.cryptotrader.simulator.library.communication.response.DetailedPortfolioSimulationResponse
import org.cryptotrader.simulator.library.communication.response.PortfolioSimulationResponse
import org.cryptotrader.simulator.library.events.DetailedSimulatorRequestGateway
import org.cryptotrader.simulator.library.events.SimulatorEventBinding
import org.cryptotrader.simulator.library.events.SimulatorRequestGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.temporal.ChronoUnit

@RequestMapping("/api/simulator")
@RestController
class SimulatorController @Autowired constructor(
    private val simulatorRequestGateway: SimulatorRequestGateway,
    private val detailedSimulatorRequestGateway: DetailedSimulatorRequestGateway
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SimulatorController::class.java)
        private val MIN_TIMEOUT: Duration = Duration.ofSeconds(15)
        private const val SECONDS_PER_DAY: Long = 1
    }

    // Temporary permit for testing.
    @PermitAll
    @PostMapping("/simulate")
    fun simulate(@RequestBody simulationRequest: PortfolioSimulationRequest): ResponseEntity<PortfolioSimulationResponse> {
        this.logSimulationRequest(simulationRequest)
        try {
            val timeout = calculateTimeout(simulationRequest)
            val result: PortfolioSimulationResponse = this.simulatorRequestGateway.execute(
                SimulatorEventBinding.PORTFOLIO_SIMULATION_REQUESTS,
                simulationRequest,
                timeout
            )
            log.info("Simulation result: {}", result)
            return ResponseEntity.ok(result)
        } catch (exception: Exception) {
            log.error("Error simulating portfolio: {}", exception.toString())
            return ResponseEntity.badRequest().build()
        }
    }

    private fun logSimulationRequest(simulationRequest: PortfolioSimulationRequest) {
        log.info(
            """Simulating portfolio:
                |Start Date: ${simulationRequest.startDate}
                |End Date: ${simulationRequest.endDate}
                |
                |Asset Simulation Requests: ${simulationRequest.assetSimulationRequests.size}
                |${
                simulationRequest.assetSimulationRequests.map {
                    """
                    ${it.currencyCode}
                    Shares: ${it.numShares}
                    Dollars: ${it.numDollars}
                """.trimIndent()
                }
            }
                |""".trimMargin()
        )
    }


    @PermitAll
    @PostMapping("/simulate/detailed")
    fun simulateDetailed(@RequestBody simulationRequest: DetailedPortfolioSimulationRequest): ResponseEntity<DetailedPortfolioSimulationResponse> {
        this.logSimulationRequest(simulationRequest)
        try {
            val timeout = calculateTimeout(simulationRequest)
            val result: DetailedPortfolioSimulationResponse = this.detailedSimulatorRequestGateway.execute(
                SimulatorEventBinding.DETAILED_PORTFOLIO_SIMULATION_REQUESTS,
                simulationRequest,
                timeout
            )
            log.info("Simulation result: {}", result)
            return ResponseEntity.ok(result)
        } catch (exception: Exception) {
            log.error("Error simulating portfolio: {}", exception.toString())
            return ResponseEntity.badRequest().build()
        }
    }

    private fun calculateTimeout(request: PortfolioSimulationRequest): Duration {
        val days = ChronoUnit.DAYS.between(request.startDate, request.endDate)
        val calculatedTimeout = Duration.ofSeconds(days * SECONDS_PER_DAY)
        return if (calculatedTimeout > MIN_TIMEOUT) calculatedTimeout else MIN_TIMEOUT
    }
}
