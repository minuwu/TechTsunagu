package com.techtsunagu.wakatta.core.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeMachineRepositoryTest {

    private val repository = FakeMachineRepository(delayMs = 0L)

    @Test
    fun getMachines_returnsSeedMachines() = runTest {
        val result = repository.getMachines()
        assertTrue(result is AppResult.Success)
        val machines = (result as AppResult.Success).value
        assertEquals(3, machines.size)
        assertEquals("mc1", machines[0].id)
    }

    @Test
    fun getMachine_validId_returnsDetailWithStepsAndQuestionsWithoutAnswers() = runTest {
        val result = repository.getMachine("mc1")
        assertTrue(result is AppResult.Success)
        val detail = (result as AppResult.Success).value
        assertEquals("mc1", detail.id)
        assertTrue(detail.steps.isNotEmpty())
        assertTrue(detail.questions.isNotEmpty())

        // Verify questions have prompt and options
        for (q in detail.questions) {
            assertNotNull(q.prompt)
            assertTrue(q.options.size >= 2)
        }
    }

    @Test
    fun getMachine_unknownId_returnsNotFound() = runTest {
        val result = repository.getMachine("non_existent")
        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
    }

    @Test
    fun scan_existingMachine_returnsSuccess() = runTest {
        val result = repository.scan("mc1")
        assertTrue(result is AppResult.Success)
    }

    @Test
    fun scan_unknownMachine_returnsNotFound() = runTest {
        val result = repository.scan("unknown_machine")
        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
    }
}
