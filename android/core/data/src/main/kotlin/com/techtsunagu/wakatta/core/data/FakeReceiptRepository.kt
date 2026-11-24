package com.techtsunagu.wakatta.core.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.ReceiptRepository
import com.techtsunagu.wakatta.core.model.Receipts
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.WorkerInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeReceiptRepository(
    private val delayMs: Long = 200L
) : ReceiptRepository {

    @Inject
    constructor() : this(200L)


    private val openedTargets = mutableSetOf<String>()

    override suspend fun markOpened(targetKind: CheckTargetKind, targetId: String): AppResult<Unit> {
        if (delayMs > 0) delay(delayMs)
        openedTargets.add("${targetKind}_$targetId")
        return AppResult.Success(Unit)
    }

    override suspend fun getReceipts(targetKind: CheckTargetKind, targetId: String): AppResult<Receipts> {
        if (delayMs > 0) delay(delayMs)
        val workers = DemoSeed.users
            .filter { it.role == Role.WORKER }
            .map { WorkerInfo(id = it.id, displayName = it.displayName, language = it.language) }

        val isOpened = openedTargets.contains("${targetKind}_$targetId")
        val receipts = if (isOpened) {
            Receipts(
                understood = listOf(workers[0]),
                openedOnly = if (workers.size > 1) listOf(workers[1]) else emptyList(),
                notOpened = if (workers.size > 2) workers.drop(2) else emptyList()
            )
        } else {
            Receipts(
                understood = emptyList(),
                openedOnly = emptyList(),
                notOpened = workers
            )
        }
        return AppResult.Success(receipts)
    }
}
