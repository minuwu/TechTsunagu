package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Shift
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.machines.domain.ShiftAssignmentDto
import com.techtsunagu.wakatta.feature.machines.domain.ShiftAssignmentRepository

internal class RealShiftAssignmentRepository(
    private val api: () -> MachinesApi,
    private val workers: WorkerRepository,
) : ShiftAssignmentRepository {

    override suspend fun getAssignments(day: String?): AppResult<List<ShiftAssignmentDto>> {
        val list = when (val result = safeApiCall { api().assignments(day) }) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        val names = if (list.isEmpty()) emptyMap() else workerNames()
        return AppResult.Success(list.map { it.toDto(names[it.workerId]) })
    }

    override suspend fun assign(
        machineId: String,
        shift: Shift,
        date: String?,
        workerId: String?,
    ): AppResult<ShiftAssignmentDto?> {
        val body = AssignmentInDto(machineId, shift.name.lowercase(), date, workerId)
        return when (val result = safeApiCall { api().assign(body) }) {
            is AppResult.Success -> AppResult.Success(result.value.toDto(workerNames()[result.value.workerId]))
            // Clearing a slot answers `data: null`, which safeApiCall reports as an empty body.
            is AppResult.Failure ->
                if (workerId == null && result.error == AppError.Unknown("empty body")) AppResult.Success(null) else result
        }
    }

    /** The assignment has only a worker id; a Worker session may not list Workers, so its names stay null. */
    private suspend fun workerNames(): Map<String, String> =
        (workers.getWorkers() as? AppResult.Success)?.value.orEmpty().associate { it.id to it.displayName }

    private fun AssignmentDto.toDto(name: String?) = ShiftAssignmentDto(
        id = id,
        machineId = machineId,
        workerId = workerId,
        workerName = name,
        shift = Shift.entries.firstOrNull { it.name.equals(shift, ignoreCase = true) } ?: Shift.UNKNOWN,
        date = date,
    )
}
