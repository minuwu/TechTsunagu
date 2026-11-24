package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.insights.domain.SkillCell
import com.techtsunagu.wakatta.feature.insights.domain.SkillMachine
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrix
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrixRepository
import com.techtsunagu.wakatta.feature.insights.domain.SkillRow
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

internal class RealSkillMatrixRepository(
    private val api: () -> InsightsApi,
    private val now: () -> Instant = Instant::now,
) : SkillMatrixRepository {

    override suspend fun getMatrix(): AppResult<SkillMatrix> {
        val dto = when (val r = safeApiCall { api().skillMatrix() }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }

        val currentTime = now()
        val machines = dto.machines.map { SkillMachine(it.id, it.name) }
        val rows = dto.rows.map { rowDto ->
            val cells = rowDto.cells.map { cellDto ->
                val status = when (cellDto.status.lowercase()) {
                    "valid" -> SkillStatus.VALID
                    "expiring" -> SkillStatus.EXPIRING
                    "expired" -> SkillStatus.EXPIRED
                    else -> SkillStatus.NONE
                }
                val passedInstant = cellDto.passedAt?.let {
                    try {
                        Instant.parse(it)
                    } catch (_: Exception) {
                        null
                    }
                }
                val signedAtMillis = passedInstant?.toEpochMilli()
                val expiresInstant = passedInstant?.plus(365, ChronoUnit.DAYS)
                val expiresAtMillis = expiresInstant?.toEpochMilli()
                val daysLeft = if (expiresInstant != null) {
                    Duration.between(currentTime, expiresInstant).toDays().toInt().coerceAtLeast(0)
                } else {
                    null
                }

                SkillCell(
                    machineId = cellDto.machineId,
                    status = status,
                    signedAtMillis = signedAtMillis,
                    expiresAtMillis = expiresAtMillis,
                    daysLeft = daysLeft,
                    version = null,
                )
            }
            SkillRow(
                workerId = rowDto.workerId,
                workerName = rowDto.displayName,
                cells = cells,
            )
        }

        return AppResult.Success(SkillMatrix(machines, rows))
    }
}
