package com.techtsunagu.wakatta.core.data.real

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.ReceiptRepository
import com.techtsunagu.wakatta.core.model.Receipts
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit

class RealReceiptRepository(private val api: () -> SharedDataApi) : ReceiptRepository {

    /** The server accepts only a Briefing or a Notice. */
    override suspend fun markOpened(targetKind: CheckTargetKind, targetId: String): AppResult<Unit> =
        when (targetKind) {
            CheckTargetKind.BRIEFING, CheckTargetKind.NOTICE ->
                safeApiCallUnit { api().markOpened(ReceiptInDto(TargetDto(targetKind.name.lowercase(), targetId))) }
            else -> AppResult.Failure(AppError.Validation(listOf("kind")))
        }

    /**
     * shortcut: only Briefing receipts are readable by a Foreman (Notice reach is Poster-only), wire when the backend ships it.
     * shortcut: the receipt list has no language per Worker, so WorkerInfo.language stays empty.
     */
    override suspend fun getReceipts(targetKind: CheckTargetKind, targetId: String): AppResult<Receipts> {
        if (targetKind != CheckTargetKind.BRIEFING) return AppResult.Failure(AppError.NotFound)
        return safeApiCall { api().briefingReceipts(targetId) }.map {
            Receipts(it.understood.toInfo(), it.openedOnly.toInfo(), it.notOpened.toInfo())
        }
    }

    private fun List<ReceiptWorkerDto>.toInfo() = map { WorkerInfo(it.id, it.name, "") }
}
