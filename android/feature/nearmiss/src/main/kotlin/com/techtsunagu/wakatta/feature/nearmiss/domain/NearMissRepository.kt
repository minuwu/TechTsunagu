package com.techtsunagu.wakatta.feature.nearmiss.domain

import com.techtsunagu.wakatta.core.model.AppResult

interface NearMissRepository {
    suspend fun getNearMisses(): AppResult<List<NearMissDto>>
    suspend fun getNearMiss(id: String): AppResult<NearMissDto>
    suspend fun createNearMiss(request: NearMissCreateRequest): AppResult<NearMissDto>
    suspend fun acknowledge(id: String, acknowledgedBy: String): AppResult<AckResult>
    suspend fun linkTask(nearMissId: String, taskId: String): AppResult<NearMissDto>
}
