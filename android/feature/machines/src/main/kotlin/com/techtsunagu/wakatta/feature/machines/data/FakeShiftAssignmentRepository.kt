package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Shift
import com.techtsunagu.wakatta.feature.machines.domain.ShiftAssignmentDto
import com.techtsunagu.wakatta.feature.machines.domain.ShiftAssignmentRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeShiftAssignmentRepository(
    private val delayMs: Long = 200L
) : ShiftAssignmentRepository {

    @Inject
    constructor() : this(200L)

    private val assignments = mutableMapOf<String, ShiftAssignmentDto>().apply {
        put("mc1", ShiftAssignmentDto("sa_1", "mc1", "u_rahim", "Rahim Uddin", Shift.DAY, "2026-11-23"))
        put("mc2", ShiftAssignmentDto("sa_2", "mc2", "u_nguyen", "Nguyen Van An", Shift.DAY, "2026-11-23"))
        put("mc3", ShiftAssignmentDto("sa_3", "mc3", "u_siti", "Siti Rahma", Shift.DAY, "2026-11-23"))
    }

    override suspend fun getAssignments(day: String?): AppResult<List<ShiftAssignmentDto>> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(assignments.values.toList())
    }

    override suspend fun assign(
        machineId: String,
        shift: Shift,
        date: String?,
        workerId: String?
    ): AppResult<ShiftAssignmentDto?> {
        if (delayMs > 0) delay(delayMs)
        val resolvedDate = date ?: "2026-11-23"

        if (workerId == null) {
            assignments.remove(machineId)
            return AppResult.Success(null)
        }

        val user = DemoSeed.users.find { it.id == workerId }
        val updated = ShiftAssignmentDto(
            id = "sa_${System.currentTimeMillis()}",
            machineId = machineId,
            workerId = workerId,
            workerName = user?.displayName ?: workerId,
            shift = shift,
            date = resolvedDate
        )
        assignments[machineId] = updated
        return AppResult.Success(updated)
    }
}
