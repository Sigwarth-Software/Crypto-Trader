package org.cryptotrader.health.library.service

import org.cryptotrader.health.library.entity.HealthStatus
import org.cryptotrader.health.library.model.CryptoTraderService
import org.cryptotrader.health.library.model.HealthStatusTimeRange
import org.cryptotrader.health.library.model.ServiceStatus
import org.cryptotrader.health.library.service.entity.HealthStatusEntityService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
open class HealthStatusService(
    val healthStatusEntityService: HealthStatusEntityService
) {
    companion object {
        const val ALIVE_THRESHOLD = 0.9
        const val DEGRADED_THRESHOLD = 0.7
        const val DEAD_THRESHOLD = 0.25
    }

    /** Returns one status per interval, ordered from oldest to newest. */
    fun getFromTimeRange(
        service: CryptoTraderService,
        timeRange: HealthStatusTimeRange
    ): List<ServiceStatus> {
        if (timeRange == HealthStatusTimeRange.NOW) {
            return listOf(
                this.healthStatusEntityService.getLatestByService(service)?.status ?: ServiceStatus.UNKNOWN
            )
        }

        val numRecords: Int = timeRange.getNumRecords()
        val endTime: LocalDateTime = LocalDateTime.now()
        val unit: ChronoUnit = if (timeRange == HealthStatusTimeRange.DAY) {
            ChronoUnit.HOURS
        } else {
            ChronoUnit.DAYS
        }
        val startTime: LocalDateTime = endTime.minus(numRecords.toLong(), unit)
        val records: List<HealthStatus> = healthStatusEntityService.getBetweenTimestamps(service, startTime, endTime)
        val buckets: Map<Int, List<HealthStatus>> = records
            .filter { !it.checkedAt.isBefore(startTime) && !it.checkedAt.isAfter(endTime) }
            .groupBy {
                // The query includes its upper boundary; keep a check at endTime in the last bucket.
                unit.between(startTime, it.checkedAt).toInt().coerceAtMost(numRecords - 1)
            }

        return List(numRecords) { index ->
            val statuses: Set<ServiceStatus> = buckets[index].orEmpty().map { it.status }.toSet()
            // Preserve incidents even if the rest of an interval was operational.
            when {
                ServiceStatus.DEAD in statuses -> ServiceStatus.DEAD
                ServiceStatus.DEGRADED in statuses -> ServiceStatus.DEGRADED
                ServiceStatus.MAINTENANCE in statuses -> ServiceStatus.MAINTENANCE
                ServiceStatus.UNKNOWN in statuses -> ServiceStatus.UNKNOWN
                ServiceStatus.ALIVE in statuses -> ServiceStatus.ALIVE
                else -> ServiceStatus.UNKNOWN
            }
        }
    }

    fun getAverageFromTimeRange(
        service: CryptoTraderService,
        timeRange: HealthStatusTimeRange
    ): ServiceStatus {
        return when (timeRange) {
            HealthStatusTimeRange.NOW -> {
                val result: HealthStatus =
                    this.healthStatusEntityService.getLatestByService(service)
                        ?: return ServiceStatus.UNKNOWN
                result.status
            }
            HealthStatusTimeRange.DAY -> {
                val today: LocalDateTime = LocalDateTime.now()
                val yesterday: LocalDateTime = today.minusDays(1)
                val healthStatuses: List<HealthStatus> =
                    this.healthStatusEntityService.getBetweenTimestamps(service, yesterday, today)
                if (healthStatuses.isEmpty()) {
                    return ServiceStatus.UNKNOWN
                }
                this.getAverageFromTimeRange(healthStatuses, service, timeRange)
            }
            HealthStatusTimeRange.WEEK -> {
                val today: LocalDateTime = LocalDateTime.now()
                val lastWeek: LocalDateTime = today.minusWeeks(1)
                val healthStatuses: List<HealthStatus> =
                    this.healthStatusEntityService.getBetweenTimestamps(service, lastWeek, today)
                if (healthStatuses.isEmpty()) {
                    return ServiceStatus.UNKNOWN
                }
                this.getAverageFromTimeRange(healthStatuses, service, timeRange)
            }
            HealthStatusTimeRange.MONTH -> {
                val today: LocalDateTime = LocalDateTime.now()
                val lastMonth: LocalDateTime = today.minusMonths(1)
                val healthStatuses: List<HealthStatus> =
                    this.healthStatusEntityService.getBetweenTimestamps(service, lastMonth, today)
                if (healthStatuses.isEmpty()) {
                    return ServiceStatus.UNKNOWN
                }
                this.getAverageFromTimeRange(healthStatuses, service, timeRange)
            }

            HealthStatusTimeRange.QUARTER -> {
                val today: LocalDateTime = LocalDateTime.now()
                val lastQuarter: LocalDateTime = today.minusMonths(3)
                val healthStatuses: List<HealthStatus> =
                    this.healthStatusEntityService.getBetweenTimestamps(service, lastQuarter, today)
                if (healthStatuses.isEmpty()) {
                    return ServiceStatus.UNKNOWN
                }
                this.getAverageFromTimeRange(healthStatuses, service, timeRange)
            }
        }
    }

    fun getAverageFromTimeRange(
        healthStatuses: List<HealthStatus>,
        service: CryptoTraderService,
        timeRange: HealthStatusTimeRange
    ): ServiceStatus {
        var aliveCount = 0
        var degradedCount = 0
        var deadCount = 0

        for ((index, healthStatus) in healthStatuses.withIndex()) {
            if (index > 0 && timeRange === HealthStatusTimeRange.NOW) {
                break
            }
            val today: LocalDateTime = LocalDateTime.now()
            val sevenDaysAgo: LocalDateTime = today.minusDays(7)

            if (healthStatus.checkedAt.isBefore(sevenDaysAgo) && timeRange === HealthStatusTimeRange.WEEK) {
                break
            }

            val oneMonthAgo: LocalDateTime = today.minusMonths(1)

            if (healthStatus.checkedAt.isBefore(oneMonthAgo) && timeRange === HealthStatusTimeRange.MONTH) {
                break
            }

            when (healthStatus.status) {
                ServiceStatus.ALIVE -> aliveCount++
                ServiceStatus.DEGRADED, ServiceStatus.UNKNOWN  -> degradedCount++
                ServiceStatus.DEAD -> deadCount++
                ServiceStatus.MAINTENANCE -> degradedCount++
            }
        }

        return if (aliveCount.toDouble() / healthStatuses.size >= ALIVE_THRESHOLD) {
            ServiceStatus.ALIVE
        } else if (degradedCount.toDouble() / healthStatuses.size >= DEGRADED_THRESHOLD) {
            ServiceStatus.DEGRADED
        } else if (deadCount.toDouble() / healthStatuses.size >= DEAD_THRESHOLD) {
            ServiceStatus.DEAD
        } else {
            ServiceStatus.MAINTENANCE
        }
    }
}