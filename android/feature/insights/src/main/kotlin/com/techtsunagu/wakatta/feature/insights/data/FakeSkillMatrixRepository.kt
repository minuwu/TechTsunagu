package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.feature.insights.domain.SkillCell
import com.techtsunagu.wakatta.feature.insights.domain.SkillMachine
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrix
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrixRepository
import com.techtsunagu.wakatta.feature.insights.domain.SkillRow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeSkillMatrixRepository(
    private val delayMs: Long,
    private val nowMillis: () -> Long,
) : SkillMatrixRepository {

    @Inject
    constructor() : this(300L, System::currentTimeMillis)

    override suspend fun getMatrix(): AppResult<SkillMatrix> {
        delay(delayMs)
        val now = nowMillis()
        val machines = DemoSeed.machines.map { SkillMachine(it.id, it.name) }
        val rows = DemoSeed.users.filter { it.role == Role.WORKER }.map { u ->
            SkillRow(u.id, u.displayName, machines.map { m -> cell(u.id, m.id, now) })
        }
        return AppResult.Success(SkillMatrix(machines, rows))
    }

    private fun cell(workerId: String, machineId: String, now: Long): SkillCell {
        val age = InsightsSeed.signOffAgeDays[workerId to machineId]
            ?: return SkillCell(machineId, SkillStatus.NONE, null, null, null, null)
        val left = InsightsSeed.SIGN_OFF_VALID_DAYS - age
        val status = when {
            left < 0 -> SkillStatus.EXPIRED
            left <= InsightsSeed.EXPIRING_DAYS -> SkillStatus.EXPIRING
            else -> SkillStatus.VALID
        }
        return SkillCell(
            machineId = machineId,
            status = status,
            signedAtMillis = now - age * InsightsSeed.MILLIS_PER_DAY,
            expiresAtMillis = now + left * InsightsSeed.MILLIS_PER_DAY,
            daysLeft = left,
            version = "v1",
        )
    }
}
