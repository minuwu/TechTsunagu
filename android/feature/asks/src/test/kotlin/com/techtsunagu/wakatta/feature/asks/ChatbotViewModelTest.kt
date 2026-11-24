package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.testing.MainDispatcherRule
import com.techtsunagu.wakatta.feature.asks.data.AskRepository
import com.techtsunagu.wakatta.feature.asks.data.ChatbotRepository
import com.techtsunagu.wakatta.feature.asks.data.FakeChatbotRepository
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskDto
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeAnswerDto
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeReplyKind
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatbotViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class Store : SessionStore {
        private val s = MutableStateFlow<Session?>(Session(token = "tok", user = DemoSeed.demoWorker))
        override val session: StateFlow<Session?> = s.asStateFlow()
        override suspend fun save(session: Session) { s.value = session }
        override suspend fun clear() { s.value = null }
        override fun getCachedToken(): String? = s.value?.token
    }

    /** Records what the screen asked, and answers with [reply] (or fails with [error]). */
    private class Bot(var reply: KnowledgeAnswerDto? = null, var error: AppError? = null) : ChatbotRepository {
        val machineIds = mutableListOf<String?>()
        override suspend fun ask(text: String?, audio: File?, machineId: String?): AppResult<KnowledgeAnswerDto> {
            machineIds += machineId
            return error?.let { AppResult.Failure(it) } ?: AppResult.Success(reply ?: KnowledgeAnswerDto(text.orEmpty(), covered = false))
        }
    }

    private class Asks : AskRepository {
        val sent = mutableListOf<String>()
        override fun getAsks(workerId: String?): Flow<List<AskDto>> = flowOf(emptyList())
        override suspend fun sendAsk(workerId: String, request: AskCreateRequest): AppResult<AskDto> {
            sent += request.text
            return AppResult.Success(AskDto(id = "9", workerId = workerId, workerName = "", text = request.text, createdAt = ""))
        }
        override suspend fun forwardAsk(askId: String): AppResult<AskDto> = AppResult.Failure(AppError.NotFound)
    }

    private fun viewModel(bot: ChatbotRepository = FakeChatbotRepository(), asks: Asks = Asks()) =
        ChatbotViewModel(bot, asks, Store())

    private fun ChatbotViewModel.ask(text: String) {
        onEvent(ChatbotEvent.InputChanged(text))
        onEvent(ChatbotEvent.Send)
    }

    @Test
    fun aCoveredAnswerHasSourcesAndAPracticeQuestionGradedOnThePhone() = runTest {
        val vm = viewModel()
        vm.ask("Why must I stop the machine before cleaning it?")
        assertTrue(vm.state.value.sending)
        advanceUntilIdle()

        val turn = vm.state.value.turns.single()
        assertEquals(KnowledgeReplyKind.COVERED, turn.kind)
        assertEquals(15, turn.reply.sources.single().page)
        assertFalse(vm.state.value.sending)
        assertEquals("", vm.state.value.input)

        vm.onEvent(ChatbotEvent.PickOption(turn.id, 1))
        assertEquals(false, vm.state.value.turns.single().pickedRight)
        vm.onEvent(ChatbotEvent.PickOption(turn.id, 0))
        assertEquals(1, vm.state.value.turns.single().picked) // the first pick counts
    }

    @Test
    fun anEmergencyIsTheFixedCardWithoutAQuestion() = runTest {
        val vm = viewModel()
        vm.ask("My friend is bleeding")
        advanceUntilIdle()

        val turn = vm.state.value.turns.single()
        assertEquals(KnowledgeReplyKind.EMERGENCY, turn.kind)
        assertTrue(turn.reply.answer!!.getValue("en").contains("119"))
        assertNull(turn.reply.check)
    }

    @Test
    fun notCoveredCanGoToTheSenpaiOnce() = runTest {
        val asks = Asks()
        val vm = viewModel(asks = asks)
        vm.ask("Can you recommend a ramen shop?")
        advanceUntilIdle()
        val turn = vm.state.value.turns.single()
        assertEquals(KnowledgeReplyKind.NOT_COVERED, turn.kind)

        vm.onEvent(ChatbotEvent.AskSenpai(turn.id))
        advanceUntilIdle()
        vm.onEvent(ChatbotEvent.AskSenpai(turn.id))
        advanceUntilIdle()

        assertTrue(vm.state.value.turns.single().forwarded)
        assertEquals(listOf("Can you recommend a ramen shop?"), asks.sent)
    }

    @Test
    fun theMachineScreenSendsItsMachine() = runTest {
        val bot = Bot()
        val vm = viewModel(bot)
        vm.onEvent(ChatbotEvent.SetMachine("2"))
        vm.ask("What must I wear?")
        advanceUntilIdle()
        assertEquals(listOf<String?>("2"), bot.machineIds)
    }

    @Test
    fun aFailedQuestionGoesBackInTheBoxWithTheError() = runTest {
        val vm = viewModel(Bot(error = AppError.Timeout))
        vm.ask("When is the drill?")
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(AppError.Timeout, s.error)
        assertEquals("When is the drill?", s.input)
        assertTrue(s.turns.isEmpty())
        vm.onEvent(ChatbotEvent.DismissError)
        assertNull(vm.state.value.error)
    }

    @Test
    fun anEmptyQuestionIsNotSent() = runTest {
        val bot = Bot()
        val vm = viewModel(bot)
        vm.ask("   ")
        advanceUntilIdle()
        assertTrue(bot.machineIds.isEmpty())
    }
}
