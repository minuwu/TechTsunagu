package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.media.RecordingLimits
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeAnswerDto
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeCheckDto
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeSourceDto
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/** The Wakatta? chatbot (XREQ-016). Nothing is stored on the server: the conversation lives in the screen. */
interface ChatbotRepository {
    /**
     * A typed [text] or a recorded [audio] question. [machineId] from a Machine screen makes that Machine's
     * instruction rank first. A model failure is `covered = false`, never an error; errors are network or 4xx only.
     */
    suspend fun ask(text: String?, audio: File?, machineId: String?): AppResult<KnowledgeAnswerDto>
}

internal interface KnowledgeApi {
    /** Multipart: `text` or `audio`, optional `machineId`. Worker only. */
    @Multipart
    @POST(ApiPaths.KNOWLEDGE_ASKS)
    suspend fun ask(@Part parts: List<MultipartBody.Part>): Response<ApiEnvelope<KnowledgeAnswerDto>>
}

internal class RealChatbotRepository(private val api: () -> KnowledgeApi) : ChatbotRepository {

    override suspend fun ask(text: String?, audio: File?, machineId: String?): AppResult<KnowledgeAnswerDto> {
        val question = audio?.let {
            MultipartBody.Part.createFormData("audio", it.name, it.asRequestBody(RecordingLimits.MIME_TYPE.toMediaType()))
        } ?: text?.trim()?.takeIf { it.isNotEmpty() }?.let { MultipartBody.Part.createFormData("text", it) }
            ?: return AppResult.Failure(AppError.Validation(listOf("text")))
        val parts = listOfNotNull(question, machineId?.let { MultipartBody.Part.createFormData("machineId", it) })
        return safeApiCall { api().ask(parts) }
    }
}

/**
 * Offline demo: the same shape as the server for the filmed questions (`backend/app/ai/demo_warmup.py`), the fixed
 * emergency card, and "not covered" for anything else.
 */
class FakeChatbotRepository @Inject constructor() : ChatbotRepository {

    override suspend fun ask(text: String?, audio: File?, machineId: String?): AppResult<KnowledgeAnswerDto> {
        delay(FAKE_DELAY_MS)
        val q = (if (audio != null) "Why must I stop the machine before cleaning it?" else text?.trim()).orEmpty()
        if (q.isEmpty()) return AppResult.Failure(AppError.Validation(listOf("text")))
        val lower = q.lowercase()
        val answer = when {
            EMERGENCY.containsMatchIn(lower) ->
                KnowledgeAnswerDto(q, covered = true, emergency = true, answer = EMERGENCY_CARD)
            listOf("clean", "掃除", "stop the machine").any { it in lower } -> KnowledgeAnswerDto(
                question = q,
                covered = true,
                answer = mapOf(
                    "ja" to "機械を掃除するときは、必ず先に機械を完全に止めてください。動いている機械に手を入れると、巻き込まれて大けがをします。",
                    "en" to "Always stop the machine completely before you clean it. A hand put into a moving machine can be caught and badly injured.",
                ),
                sources = listOf(MHLW_P15),
                check = KnowledgeCheckDto(
                    prompt = mapOf("ja" to "機械を掃除する前に、何をしますか？", "en" to "What do you do before cleaning the machine?"),
                    options = listOf(
                        mapOf("ja" to "機械を完全に止める", "en" to "Stop the machine completely"),
                        mapOf("ja" to "ゆっくり動かしておく", "en" to "Keep it running slowly"),
                        mapOf("ja" to "手袋だけつける", "en" to "Only put on gloves"),
                    ),
                    correctIndex = 0,
                ),
            )
            else -> KnowledgeAnswerDto(q, covered = false)
        }
        return AppResult.Success(answer)
    }

    private companion object {
        const val FAKE_DELAY_MS = 600L
        // Mirrors backend app/domain/retrieval.py is_emergency (a short list is enough for the demo).
        val EMERGENCY = Regex("""怪我|けが|出血|火事|火災|ガス漏れ|救急|倒れ|挟まれ|\binjur|\bbleed|\bblood\b|\bfire\b|gas leak|\b119\b|\bemergency\b""")
        val EMERGENCY_CARD = mapOf(
            "ja" to "すぐに作業を止めて、まわりの人とフォアマンに大きな声で知らせてください。けが・火事・ガスのときは 119 に電話してください。",
            "en" to "Stop work now and shout to the people around you and your Foreman. For an injury, fire or gas, call 119.",
        )
        val MHLW_P15 = KnowledgeSourceDto(
            id = "mhlw-safety:p15:1",
            kind = "safety",
            title = "For Work Safety and Health: To those working in the manufacturing industry",
            page = 15,
            url = "https://www.mhlw.go.jp/file/06-Seisakujouhou-11300000-Roudoukijunkyokuanzeneiseibu/0000148471.pdf",
            attribution = "出典：厚生労働省「未熟練労働者に対する安全衛生教育マニュアル（製造業・英語）」をもとに Wakatta? が抜粋・要約",
        )
    }
}
