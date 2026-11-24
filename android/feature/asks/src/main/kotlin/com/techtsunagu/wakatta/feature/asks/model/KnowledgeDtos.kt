package com.techtsunagu.wakatta.feature.asks.model

import com.techtsunagu.wakatta.core.model.Tri
import kotlinx.serialization.Serializable

/** Where a chatbot answer came from (`POST /api/knowledge/asks`, XREQ-016). */
@Serializable
data class KnowledgeSourceDto(
    val id: String,
    /** `safety`, `life`, `note` (the shop's instruction) or `notice`. */
    val kind: String,
    val title: String,
    /** Page of the guide's PDF; null for the shop's instruction or a Notice. */
    val page: Int? = null,
    /** Opens the guide; null for the shop's instruction or a Notice. */
    val url: String? = null,
    /** Show it under the answer when not null: the guides' licence asks for it. */
    val attribution: String? = null,
)

/** The practice question after an answer. Graded on the phone with [correctIndex]; it is not a Check. */
@Serializable
data class KnowledgeCheckDto(
    val id: String = "q1",
    val termId: String? = null,
    val prompt: Tri,
    val options: List<Tri>,
    val correctIndex: Int,
)

/**
 * The chatbot's answer: only from cited sources. [emergency] means [answer] is the fixed emergency card (never written
 * by AI); not [covered] means no source answers it, so the app offers "Ask my Senpai".
 */
@Serializable
data class KnowledgeAnswerDto(
    val question: String,
    val covered: Boolean,
    val emergency: Boolean = false,
    val answer: Tri? = null,
    val sources: List<KnowledgeSourceDto> = emptyList(),
    val check: KnowledgeCheckDto? = null,
)

/** The three ways an answer is shown (the fourth state, waiting, is [com.techtsunagu.wakatta.feature.asks.ChatbotUiState.sending]). */
enum class KnowledgeReplyKind { COVERED, NOT_COVERED, EMERGENCY }

val KnowledgeAnswerDto.kind: KnowledgeReplyKind
    get() = when {
        emergency -> KnowledgeReplyKind.EMERGENCY
        covered && answer != null -> KnowledgeReplyKind.COVERED
        else -> KnowledgeReplyKind.NOT_COVERED
    }
