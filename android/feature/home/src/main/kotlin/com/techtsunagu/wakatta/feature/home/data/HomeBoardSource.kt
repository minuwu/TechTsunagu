package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/** A Machine with a Worker assigned without a valid Sign-off (`GET /board`). */
data class BoardLocked(val machineId: String, val name: Tri, val workerNames: List<String>)

data class HomeFollowUp(val id: String, val workerName: String, val termJa: String)

data class HomeTally(val green: Int = 0, val orange: Int = 0, val red: Int = 0)

data class HomeTeamMember(val id: String, val name: String, val permitted: Int, val total: Int)

data class HomeBriefing(val id: String, val text: Tri)

/** A message with a Check Question that this Worker has not answered. */
data class HomePendingCheck(val id: String, val threadId: String, val threadTitle: String, val text: Tri, val sentAt: String)

interface HomeBoardSource {
    /** Foreman. */
    suspend fun getLockedMachines(): AppResult<List<BoardLocked>>

    /** Foreman: open Follow-ups, newest first. */
    suspend fun getOpenFollowUps(): AppResult<List<HomeFollowUp>>

    /** Foreman: answers of today. */
    suspend fun getTodayTally(): AppResult<HomeTally>

    /** Foreman: the Workers with the number of Machines they may use. */
    suspend fun getTeam(): AppResult<List<HomeTeamMember>>

    /** Worker: the newest published Briefing. */
    suspend fun getBriefing(): AppResult<HomeBriefing?>

    /** Worker. */
    suspend fun getPendingChecks(): AppResult<List<HomePendingCheck>>

    /** Ends the session on the server. The caller clears the local one. */
    suspend fun signOut(): AppResult<Unit>
}

@Singleton
class FakeHomeBoardSource @Inject constructor() : HomeBoardSource {
    override suspend fun getLockedMachines(): AppResult<List<BoardLocked>> =
        AppResult.Success(listOf(BoardLocked("mc2", mapOf("ja" to "プレス2号機", "en" to "Press #2"), listOf("Rahim", "Nguyen"))))

    override suspend fun getOpenFollowUps(): AppResult<List<HomeFollowUp>> =
        AppResult.Success(listOf(HomeFollowUp("f1", "Rahim Uddin", "金型交換")))

    override suspend fun getTodayTally(): AppResult<HomeTally> = AppResult.Success(HomeTally(5, 2, 1))

    override suspend fun getTeam(): AppResult<List<HomeTeamMember>> =
        AppResult.Success(listOf(HomeTeamMember("u_rahim", "Rahim Uddin", 2, 3)))

    override suspend fun getBriefing(): AppResult<HomeBriefing?> =
        AppResult.Success(HomeBriefing("b1", mapOf("en" to "Line 2 press die maintenance safety protocols")))

    override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun getPendingChecks(): AppResult<List<HomePendingCheck>> = AppResult.Success(
        listOf(
            HomePendingCheck("m1", "t_line2", "2号ライン", mapOf("ja" to "今日は3号機の油の点検を先にしてください。"), "2026-11-24T08:02:00+09:00"),
        ),
    )
}

internal interface HomeApi {
    /** Foreman only. */
    @GET(ApiPaths.BOARD)
    suspend fun board(): Response<ApiEnvelope<WireBoard>>

    /** Foreman only. */
    @GET(ApiPaths.FOLLOW_UPS)
    suspend fun followUps(@Query("limit") limit: Int): Response<ApiEnvelope<WireItems<WireFollowUp>>>

    /** Foreman only. */
    @GET(ApiPaths.ANALYTICS_UNDERSTANDING)
    suspend fun understanding(@Query("days") days: Int): Response<ApiEnvelope<WireUnderstanding>>

    /** Foreman only. */
    @GET(ApiPaths.SKILL_MATRIX)
    suspend fun skills(): Response<ApiEnvelope<WireSkills>>

    @POST(ApiPaths.LOGOUT)
    suspend fun logout(): Response<ApiEnvelope<Unit>>

    @GET(ApiPaths.BRIEFINGS)
    suspend fun briefings(@Query("limit") limit: Int): Response<ApiEnvelope<WireItems<WireBriefing>>>

    /** Worker: open Checks across every Thread in one call; listing them marks nothing as read (S3). */
    @GET(ApiPaths.MESSAGES_UNANSWERED)
    suspend fun unanswered(): Response<ApiEnvelope<List<WireUnanswered>>>
}

@Serializable
internal data class WireItems<T>(val items: List<T> = emptyList())

@Serializable
internal data class WireBoard(val machines: List<WireBoardMachine> = emptyList())

@Serializable
internal data class WireBoardMachine(
    val id: String,
    val name: Tri = emptyMap(),
    val assignments: List<WireAssignment> = emptyList(),
)

@Serializable
internal data class WireAssignment(val displayName: String = "", val flag: Boolean = false)

@Serializable
internal data class WireFollowUp(val id: String, val displayName: String = "", val termJa: String = "", val status: String)

@Serializable
internal data class WireUnderstanding(val trend: List<WireDay> = emptyList())

@Serializable
internal data class WireDay(val green: Int = 0, val orange: Int = 0, val red: Int = 0)

@Serializable
internal data class WireSkills(val rows: List<WireSkillRow> = emptyList())

@Serializable
internal data class WireSkillRow(val workerId: String, val displayName: String = "", val cells: List<WireCell> = emptyList())

@Serializable
internal data class WireCell(val status: String)

@Serializable
internal data class WireBriefing(val id: String, val text: Tri = emptyMap(), val status: String)

/** `GET /messages/unanswered`: the Worker's view only, with no answer key (rule R-3). */
@Serializable
internal data class WireUnanswered(
    val messageId: String,
    val threadId: String,
    val threadTitle: String = "",
    val text: Tri = emptyMap(),
    val sentAt: String,
    val deadline: String? = null,
)

internal class RealHomeBoardSource(private val api: () -> HomeApi) : HomeBoardSource {
    override suspend fun getLockedMachines(): AppResult<List<BoardLocked>> = safeApiCall { api().board() }.map { board ->
        board.machines.filter { m -> m.assignments.any { it.flag } }.map { m ->
            BoardLocked(m.id, m.name, m.assignments.filter { it.flag }.map { it.displayName }.distinct())
        }
    }

    override suspend fun getOpenFollowUps(): AppResult<List<HomeFollowUp>> = safeApiCall { api().followUps(PAGE) }.map { page ->
        page.items.filter { it.status == "open" }.map { HomeFollowUp(it.id, it.displayName, it.termJa) }
    }

    override suspend fun getTodayTally(): AppResult<HomeTally> = safeApiCall { api().understanding(days = 1) }.map { u ->
        u.trend.lastOrNull()?.let { HomeTally(it.green, it.orange, it.red) } ?: HomeTally()
    }

    override suspend fun getTeam(): AppResult<List<HomeTeamMember>> = safeApiCall { api().skills() }.map { skills ->
        skills.rows.map { r ->
            HomeTeamMember(r.workerId, r.displayName, r.cells.count { it.status == "valid" || it.status == "expiring" }, r.cells.size)
        }
    }

    override suspend fun getBriefing(): AppResult<HomeBriefing?> = safeApiCall { api().briefings(PAGE) }.map { page ->
        page.items.firstOrNull { it.status == "published" }?.let { HomeBriefing(it.id, it.text) }
    }

    override suspend fun signOut(): AppResult<Unit> = safeApiCallUnit { api().logout() }

    override suspend fun getPendingChecks(): AppResult<List<HomePendingCheck>> =
        safeApiCall { api().unanswered() }.map { items ->
            items.map { HomePendingCheck(it.messageId, it.threadId, it.threadTitle, it.text, it.sentAt) }
        }

    private fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Failure -> this
    }

    private companion object {
        const val PAGE = 50
    }
}
