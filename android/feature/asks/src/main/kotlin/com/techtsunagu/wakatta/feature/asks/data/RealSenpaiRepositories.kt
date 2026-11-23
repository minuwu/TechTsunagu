package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.asks.model.MeetupCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.MeetupDto
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

private const val PAGE_LIMIT = 200

private fun SessionStore.worker(): User? = session.value?.user?.takeIf { it.role == Role.WORKER }

/** The app says "festival", the server says "event". */
private fun String.toWire() = if (this == "festival") "event" else this
private fun String.fromWire() = if (this == "event") "festival" else this

/**
 * A Worker's own Senpai pair (`GET /senpai-pairs/me`). The Foreman list, create and revoke keep the [fake]:
 * those routes exist but are not wired in this task.
 * shortcut: Foreman pair management is Fake, wire GET|POST /senpai-pairs and DELETE /senpai-pairs/{id} next.
 */
internal class RealSenpaiRepository(
    private val api: () -> AsksApi,
    private val session: SessionStore,
    private val fake: SenpaiRepository,
) : SenpaiRepository by fake {

    override fun getSenpaiPairForWorker(workerId: String): Flow<SenpaiPairDto?> {
        val user = session.worker() ?: return fake.getSenpaiPairForWorker(workerId)
        return flow { emit(loadPair(user)) }
    }

    private suspend fun loadPair(user: User): SenpaiPairDto? {
        val pair = when (val r = safeApiCall { api().myPair() }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return null // no Senpai (data: null) or a failed call: the screen shows "no Senpai yet"
        }
        val meetups = (allPages { c -> api().meetups(PAGE_LIMIT, c) } as? AppResult.Success)?.value.orEmpty()
        return SenpaiPairDto(
            id = pair.id,
            workerId = user.id,
            workerName = user.displayName,
            workerLanguage = user.language,
            senpaiName = pair.senpaiName,
            kind = pair.kind,
            meetupCount = meetups.size,
            lastMeetupDate = meetups.maxOfOrNull { it.date },
            createdAt = pair.createdAt,
            expiresAt = pair.expiresAt,
        )
    }
}

/** A Worker's own Meet-ups (`GET|POST /meetups`); a Foreman keeps the [fake] (the server needs a pair join for names). */
internal class RealMeetupRepository(
    private val api: () -> AsksApi,
    private val session: SessionStore,
    private val fake: MeetupRepository,
) : MeetupRepository by fake {

    private val meetups = MutableStateFlow<List<MeetupDto>>(emptyList())

    override fun getMeetupsForWorker(workerId: String): Flow<List<MeetupDto>> {
        val user = session.worker() ?: return fake.getMeetupsForWorker(workerId)
        return meetups.onStart { refresh(user) }
    }

    private suspend fun refresh(user: User) {
        val senpai = api().pairOrNull()
        val all = allPages { c -> api().meetups(PAGE_LIMIT, c) }
        if (all is AppResult.Success) {
            meetups.value = all.value.map {
                MeetupDto(it.id, user.id, user.displayName, it.pairId, senpai?.senpaiName.orEmpty(), it.kind.fromWire(), it.date, it.note, it.loggedBy)
            }.sortedByDescending { it.date }
        }
    }

    // shortcut: a Foreman cannot log a Meet-up on the server (Worker-only route), so that path stays Fake.
    override suspend fun logMeetup(workerId: String, request: MeetupCreateRequest): AppResult<MeetupDto> {
        val user = session.worker() ?: return fake.logMeetup(workerId, request)
        val body = MeetupInDto(request.kind.toWire(), request.date, request.note)
        val senpai = api().pairOrNull()
        return safeApiCall { api().logMeetup(IdempotencyKey(request.idempotencyKey), body) }.map {
            MeetupDto(it.id, user.id, user.displayName, it.pairId, senpai?.senpaiName.orEmpty(), it.kind.fromWire(), it.date, it.note, it.loggedBy)
                .also { m -> meetups.update { list -> (list + m).sortedByDescending { x -> x.date } } }
        }
    }
}
