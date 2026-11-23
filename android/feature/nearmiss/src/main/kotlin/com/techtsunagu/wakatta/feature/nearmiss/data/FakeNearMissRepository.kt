package com.techtsunagu.wakatta.feature.nearmiss.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.nearmiss.domain.AckResult
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissCreateRequest
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissDto
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import com.techtsunagu.wakatta.feature.nearmiss.domain.PhotoMark
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory Fake implementation of [NearMissRepository] with 250ms simulated latency
 * and canned translations ported from prototypes/wakatta/parts/C.js.
 */
@Singleton
class FakeNearMissRepository(
    private val delayMs: Long = 250L,
) : NearMissRepository {

    @Inject
    constructor() : this(250L)

    private val mutex = Mutex()
    private var simulatedError: AppError? = null

    private val seededReports = listOf(
        NearMissDto(
            id = "nm_s1",
            userId = "u_siti",
            machineId = "mc3",
            severity = NearMissSeverity.HIGH,
            status = NearMissStatus.OPEN,
            createdAt = "2026-11-23T08:00:00Z",
            text = mapOf(
                "en" to "Hot resin splashed from the nozzle when I opened the door too early.",
                "ja" to "ドアを早く開けすぎて、ノズルから熱い樹脂が飛び散りました。",
                "bn" to "দরজা খুব তাড়াতাড়ি খোলার কারণে নজল থেকে গরম রেজিন ছিটকে এসেছিল।",
            ),
            photoUrl = "https://picsum.photos/seed/nozzle/600/400",
            photoMark = PhotoMark(58f, 44f),
        ),
        NearMissDto(
            id = "nm_s2",
            userId = "u_nguyen",
            machineId = "mc1",
            severity = NearMissSeverity.LOW,
            status = NearMissStatus.ACK,
            createdAt = "2026-11-23T07:34:00Z",
            acknowledgedAt = "2026-11-23T07:42:00Z",
            acknowledgedBy = "u_tanaka",
            text = mapOf(
                "en" to "A metal chip fell from the tray and I almost slipped on it.",
                "ja" to "切粉がトレーから落ちていて、滑りそうになりました。",
                "bn" to "ট্রে থেকে একটি ধাতব চিপ পড়েছিল এবং আমি পিছলে পড়তে যাচ্ছিলাম।",
            ),
            photoUrl = "https://picsum.photos/seed/chips/600/400",
            photoMark = PhotoMark(40f, 62f),
        ),
        NearMissDto(
            id = "nm_s3",
            userId = "u_rahim",
            machineId = "mc2",
            severity = NearMissSeverity.MID,
            status = NearMissStatus.OPEN,
            createdAt = "2026-11-23T08:15:00Z",
            text = mapOf(
                "en" to "The safety guard was open when I reached in to clear a jam. My glove almost got caught between the dies.",
                "ja" to "詰まりを取ろうと手を入れたとき、安全カバーが開いていました。手袋が金型の間に挟まれそうになりました。",
                "bn" to "জ্যাম পরিষ্কার করার জন্য হাত বাড়ালে নিরাপত্তা গার্ড খোলা ছিল। আমার গ্লাভস ডাইসের মাঝে আটকে যাওয়ার উপক্রম হয়েছিল।",
            ),
            photoUrl = "https://picsum.photos/seed/nearmiss-mc2/600/400",
            photoMark = PhotoMark(56f, 46f),
        ),
    )

    private val dict = mapOf(
        "mc1" to mapOf(
            "en" to "A metal chip flew out when I opened the chuck cover. It missed my eye by a few centimeters.",
            "ja" to "チャックのカバーを開けたとき、切粉が飛び出しました。目の数センチ横を通りました。",
            "bn" to "চক কভার খোলার সময় ধাতুর টুকরো উড়ে এসেছিল। এটি আমার চোখের কয়েক সেন্টিমিটার দূর দিয়ে চলে গেছে।",
        ),
        "mc2" to mapOf(
            "en" to "The safety guard was open when I reached in to clear a jam. My glove almost got caught between the dies.",
            "ja" to "詰まりを取ろうと手を入れたとき、安全カバーが開いていました。手袋が金型の間に挟まれそうになりました。",
            "bn" to "জ্যাম পরিষ্কার করার জন্য হাত বাড়ালে নিরাপত্তা গার্ড খোলা ছিল। আমার গ্লাভস ডাইসের মাঝে আটকে যাওয়ার উপক্রম হয়েছিল।",
        ),
        "mc3" to mapOf(
            "en" to "Hot resin splashed from the nozzle when I opened the door too early.",
            "ja" to "ドアを早く開けすぎて、ノズルから熱い樹脂が飛び散りました。",
            "bn" to "দরজা খুব তাড়াতাড়ি খোলার কারণে নজল থেকে গরম রেজিন ছিটকে এসেছিল।",
        ),
    )

    private val reports = seededReports.toMutableList()

    fun simulateError(error: AppError?) {
        simulatedError = error
    }

    fun resetToDefault() {
        reports.clear()
        reports.addAll(seededReports)
        simulatedError = null
    }

    fun addReportDirectly(dto: NearMissDto) {
        reports.add(0, dto)
    }

    override suspend fun getNearMisses(): AppResult<List<NearMissDto>> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            simulatedError?.let { return@withLock AppResult.Failure(it) }
            AppResult.Success(reports.toList())
        }
    }

    override suspend fun getNearMiss(id: String): AppResult<NearMissDto> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            simulatedError?.let { return@withLock AppResult.Failure(it) }
            val item = reports.find { it.id == id }
            if (item != null) {
                AppResult.Success(item)
            } else {
                AppResult.Failure(AppError.NotFound)
            }
        }
    }

    override suspend fun createNearMiss(request: NearMissCreateRequest): AppResult<NearMissDto> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            simulatedError?.let { return@withLock AppResult.Failure(it) }
            val canned = dict[request.machineId]
            val trimmedText = request.text.trim()
            val translatedTri: Map<String, String> = if (canned != null &&
                (canned["en"]?.startsWith(trimmedText, ignoreCase = true) == true ||
                    trimmedText.startsWith(canned["en"]?.take(15) ?: "___", ignoreCase = true))
            ) {
                canned
            } else {
                mapOf(
                    "ja" to "（自動翻訳） $trimmedText",
                    "en" to trimmedText,
                    "bn" to trimmedText,
                )
            }
            val newId = "nm_" + System.currentTimeMillis().toString(36)
            val resolvedPhotoUrl = request.photoUrl ?: if (request.photoFile != null) {
                "https://picsum.photos/seed/nearmiss-${request.machineId}/600/400"
            } else {
                null
            }
            val created = NearMissDto(
                id = newId,
                userId = "u_rahim",
                machineId = request.machineId,
                text = translatedTri,
                severity = request.severity,
                photoUrl = resolvedPhotoUrl,
                photoMark = request.photoMark,
                createdAt = Instant.now().toString(),
                status = NearMissStatus.OPEN,
            )
            reports.add(0, created)
            AppResult.Success(created)
        }
    }

    override suspend fun acknowledge(id: String, acknowledgedBy: String): AppResult<AckResult> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            simulatedError?.let { return@withLock AppResult.Failure(it) }
            val index = reports.indexOfFirst { it.id == id }
            if (index == -1) return@withLock AppResult.Failure(AppError.NotFound)
            val current = reports[index]
            val now = Instant.now().toString()
            val updated = current.copy(
                status = NearMissStatus.ACK,
                acknowledgedAt = now,
                acknowledgedBy = acknowledgedBy,
            )
            reports[index] = updated
            AppResult.Success(
                AckResult(
                    id = id,
                    status = NearMissStatus.ACK,
                    acknowledgedAt = now,
                    acknowledgedBy = acknowledgedBy,
                )
            )
        }
    }

    override suspend fun linkTask(nearMissId: String, taskId: String): AppResult<NearMissDto> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            simulatedError?.let { return@withLock AppResult.Failure(it) }
            val index = reports.indexOfFirst { it.id == nearMissId }
            if (index == -1) return@withLock AppResult.Failure(AppError.NotFound)
            val current = reports[index]
            val now = Instant.now().toString()
            val updated = current.copy(
                taskId = taskId,
                status = NearMissStatus.ACK,
                acknowledgedAt = current.acknowledgedAt ?: now,
                acknowledgedBy = current.acknowledgedBy ?: "u_tanaka",
            )
            reports[index] = updated
            AppResult.Success(updated)
        }
    }
}
