package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.asks.model.MeetupCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.MeetupDto
import kotlinx.coroutines.flow.Flow

interface MeetupRepository {
    fun getMeetupsForWorker(workerId: String): Flow<List<MeetupDto>>
    fun getAllMeetups(): Flow<List<MeetupDto>>
    suspend fun logMeetup(workerId: String, request: MeetupCreateRequest): AppResult<MeetupDto>
}
