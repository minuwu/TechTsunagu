package com.techtsunagu.wakatta.feature.machines.domain

import com.techtsunagu.wakatta.core.model.AppResult

interface PanelRepository {
    suspend fun getPanelState(machineId: String, ifNoneMatch: String? = null): AppResult<PanelStateResult>
    suspend fun requestOverride(
        machineId: String,
        workerId: String?,
        minutes: Int,
        reason: String,
        idempotencyKey: String,
    ): AppResult<PanelOverrideDto>
    suspend fun revokeOverride(machineId: String, overrideId: String): AppResult<Unit>
    suspend fun recordPresence(machineId: String, workerId: String): AppResult<Unit>
    /** Asks a Worker to take the Machine's Check (Foreman); the server allows one reminder per 10 minutes. */
    suspend fun remindWorker(machineId: String, workerId: String): AppResult<Unit>
}
