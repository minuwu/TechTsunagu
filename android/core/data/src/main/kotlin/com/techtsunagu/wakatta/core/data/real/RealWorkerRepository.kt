package com.techtsunagu.wakatta.core.data.real

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository

class RealWorkerRepository(private val api: () -> SharedDataApi) : WorkerRepository {
    override suspend fun getWorkers(): AppResult<List<WorkerInfo>> =
        allPages { limit, cursor -> api().workers(limit, cursor) }
            .map { accounts -> accounts.filter { it.active }.map { WorkerInfo(it.id, it.displayName, it.language) } }
}
