package com.techtsunagu.wakatta.feature.machines.domain

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Shift

interface ShiftAssignmentRepository {
    suspend fun getAssignments(day: String? = null): AppResult<List<ShiftAssignmentDto>>
    suspend fun assign(machineId: String, shift: Shift, date: String?, workerId: String?): AppResult<ShiftAssignmentDto?>
}
