package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreatedDto
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairDto
import kotlinx.coroutines.flow.Flow

interface SenpaiRepository {
    fun getSenpaiPairForWorker(workerId: String): Flow<SenpaiPairDto?>
    fun getAllSenpaiPairs(): Flow<List<SenpaiPairDto>>
    suspend fun createSenpaiPair(request: SenpaiPairCreateRequest): AppResult<SenpaiPairCreatedDto>
    suspend fun deleteSenpaiPair(pairId: String): AppResult<Unit>
}
