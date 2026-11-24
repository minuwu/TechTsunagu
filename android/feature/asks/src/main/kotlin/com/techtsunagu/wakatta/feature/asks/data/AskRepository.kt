package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskDto
import kotlinx.coroutines.flow.Flow

interface AskRepository {
    fun getAsks(workerId: String?): Flow<List<AskDto>>
    suspend fun sendAsk(workerId: String, request: AskCreateRequest): AppResult<AskDto>
    suspend fun forwardAsk(askId: String): AppResult<AskDto>
    suspend fun replyAsk(askId: String, replyJa: String, replyEn: String, replyBn: String): AppResult<AskDto> =
        AppResult.Failure(AppError.Forbidden)
}
