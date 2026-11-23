package com.techtsunagu.wakatta.core.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeWorkerRepository(
    private val delayMs: Long = 200L
) : WorkerRepository {

    @Inject
    constructor() : this(200L)


    override suspend fun getWorkers(): AppResult<List<WorkerInfo>> {
        if (delayMs > 0) delay(delayMs)
        val workers = DemoSeed.users
            .filter { it.role == Role.WORKER }
            .map { WorkerInfo(id = it.id, displayName = it.displayName, language = it.language) }
        return AppResult.Success(workers)
    }
}
