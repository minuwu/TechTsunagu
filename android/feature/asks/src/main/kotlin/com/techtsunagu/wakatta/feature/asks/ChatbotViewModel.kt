package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.feature.asks.data.AskRepository
import com.techtsunagu.wakatta.feature.asks.data.ChatbotRepository
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeAnswerDto
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeReplyKind
import com.techtsunagu.wakatta.feature.asks.model.kind
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.launch

/** One question and its answer. [picked] is the Worker's answer to the practice question, graded on the phone. */
data class ChatbotTurn(
    val id: Long,
    val reply: KnowledgeAnswerDto,
    val picked: Int? = null,
    val forwarding: Boolean = false,
    val forwarded: Boolean = false,
) {
    val kind: KnowledgeReplyKind get() = reply.kind

    /** Null until answered; true when the picked option is the right one. */
    val pickedRight: Boolean? get() = picked?.let { it == reply.check?.correctIndex }
}

/**
 * The chatbot screen's state (XREQ-016, Android fix plan G4; the screen is M7). The four states the screen shows:
 * [sending] (waiting), and per turn [KnowledgeReplyKind.COVERED], [KnowledgeReplyKind.NOT_COVERED] (offer "Ask my
 * Senpai") and [KnowledgeReplyKind.EMERGENCY] (the fixed red card). [error] is a network or server error, not an AI one.
 */
data class ChatbotUiState(
    val input: String = "",
    /** Set from a Machine screen: that Machine's instruction ranks first. */
    val machineId: String? = null,
    val language: String = "en",
    val turns: List<ChatbotTurn> = emptyList(),
    val sending: Boolean = false,
    val error: AppError? = null,
) {
    val canSend: Boolean get() = input.isNotBlank() && !sending
}

sealed interface ChatbotEvent {
    data class SetMachine(val machineId: String?) : ChatbotEvent
    data class InputChanged(val text: String) : ChatbotEvent
    data object Send : ChatbotEvent
    data class SendRecording(val audio: File) : ChatbotEvent
    data class PickOption(val turnId: Long, val option: Int) : ChatbotEvent
    data class AskSenpai(val turnId: Long) : ChatbotEvent
    data class OpenSource(val url: String) : ChatbotEvent
    data object DismissError : ChatbotEvent
}

sealed interface ChatbotEffect {
    /** Open a guide's PDF in the browser. */
    data class OpenUrl(val url: String) : ChatbotEffect
}

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val chatbot: ChatbotRepository,
    private val asks: AskRepository,
    private val session: SessionStore,
) : MviViewModel<ChatbotUiState, ChatbotEvent, ChatbotEffect>(
    ChatbotUiState(language = session.session.value?.user?.language ?: "en"),
) {
    private var nextId = 0L

    override fun onEvent(event: ChatbotEvent) {
        when (event) {
            is ChatbotEvent.SetMachine -> updateState { it.copy(machineId = event.machineId) }
            is ChatbotEvent.InputChanged -> updateState { it.copy(input = event.text) }
            ChatbotEvent.Send -> if (state.value.canSend) ask(state.value.input.trim(), null)
            is ChatbotEvent.SendRecording -> if (!state.value.sending) ask(null, event.audio)
            is ChatbotEvent.PickOption -> pick(event.turnId, event.option)
            is ChatbotEvent.AskSenpai -> askSenpai(event.turnId)
            is ChatbotEvent.OpenSource -> sendEffect(ChatbotEffect.OpenUrl(event.url))
            ChatbotEvent.DismissError -> updateState { it.copy(error = null) }
        }
    }

    private fun ask(text: String?, audio: File?) {
        updateState { it.copy(sending = true, error = null, input = if (text != null) "" else it.input) }
        viewModelScope.launch {
            when (val res = chatbot.ask(text, audio, state.value.machineId)) {
                is AppResult.Success -> updateState {
                    it.copy(sending = false, turns = it.turns + ChatbotTurn(nextId++, res.value))
                }
                // The question goes back in the box, so the Worker can send it again.
                is AppResult.Failure -> updateState {
                    it.copy(sending = false, error = res.error, input = if (text != null && it.input.isEmpty()) text else it.input)
                }
            }
        }
    }

    private fun pick(turnId: Long, option: Int) = updateTurn(turnId) { turn ->
        if (turn.picked != null || turn.reply.check == null) turn else turn.copy(picked = option)
    }

    /** "Ask my Senpai": the existing Asks route; a question no Notice covers is forwarded to the Senpai at once. */
    private fun askSenpai(turnId: Long) {
        val turn = state.value.turns.firstOrNull { it.id == turnId } ?: return
        if (turn.forwarding || turn.forwarded || turn.kind != KnowledgeReplyKind.NOT_COVERED) return
        val workerId = session.session.value?.user?.id ?: return
        updateTurn(turnId) { it.copy(forwarding = true) }
        viewModelScope.launch {
            val res = asks.sendAsk(workerId, AskCreateRequest(text = turn.reply.question))
            updateTurn(turnId) { it.copy(forwarding = false, forwarded = res is AppResult.Success) }
            if (res is AppResult.Failure) updateState { it.copy(error = res.error) }
        }
    }

    private fun updateTurn(turnId: Long, change: (ChatbotTurn) -> ChatbotTurn) = updateState { s ->
        s.copy(turns = s.turns.map { if (it.id == turnId) change(it) else it })
    }
}
