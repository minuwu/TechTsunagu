package com.techtsunagu.wakatta.feature.briefing.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.briefing.model.BriefingDraft
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForForeman
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForWorker
import com.techtsunagu.wakatta.feature.briefing.model.BriefingPublishRequest
import com.techtsunagu.wakatta.feature.briefing.model.BriefingReceipts
import com.techtsunagu.wakatta.feature.briefing.model.BriefingWorkerStatus
import java.io.File
import kotlinx.coroutines.flow.Flow

interface BriefingRepository {
    fun observeBriefings(): Flow<List<BriefingForForeman>>
    suspend fun getBriefings(): AppResult<List<BriefingForForeman>>
    suspend fun getBriefingForForeman(id: String?): AppResult<BriefingForForeman>
    suspend fun getBriefingForWorker(id: String?): AppResult<BriefingForWorker>
    suspend fun createDraft(text: String?, audioFile: File?, idempotencyKey: String): AppResult<BriefingDraft>
    suspend fun publishBriefing(request: BriefingPublishRequest): AppResult<BriefingForForeman>
    suspend fun getReceipts(briefingId: String): AppResult<BriefingReceipts>
    suspend fun getWorkerStatuses(briefingId: String): AppResult<List<BriefingWorkerStatus>>
    suspend fun sendReexplanation(briefingId: String, workerId: String): AppResult<Unit>
    suspend fun markOpened(briefingId: String): AppResult<Unit>
    suspend fun recordWorkerAnswer(briefingId: String, workerId: String, answerIndex: Int, status: WakattaStatus): AppResult<Unit>
}
