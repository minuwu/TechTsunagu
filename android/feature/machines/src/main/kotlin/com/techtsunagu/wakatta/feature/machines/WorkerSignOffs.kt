package com.techtsunagu.wakatta.feature.machines

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.SignOff
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository

/** One Worker with their Sign-offs; a Worker is ready on a Machine with a valid or expiring Sign-off. */
internal data class WorkerSignOffs(val worker: WorkerInfo, val signOffs: List<SignOff>) {
    fun statusOn(machineId: String): SkillStatus = signOffs.find { it.machineId == machineId }?.status ?: SkillStatus.NONE

    fun isReadyOn(machineId: String): Boolean = statusOn(machineId).let { it == SkillStatus.VALID || it == SkillStatus.EXPIRING }
}

/**
 * Every Worker with their Sign-offs. A Worker who may only read their own Sign-offs gets no rows for the others
 * (the server answers 403), so such a Worker sees only themselves.
 */
internal suspend fun loadWorkerSignOffs(
    workerRepository: WorkerRepository,
    signOffRepository: SignOffRepository,
): List<WorkerSignOffs> {
    val workers = (workerRepository.getWorkers() as? AppResult.Success)?.value.orEmpty()
    return workers.mapNotNull { worker ->
        (signOffRepository.getSignOffs(worker.id) as? AppResult.Success)?.let { WorkerSignOffs(worker, it.value) }
    }
}
