package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeNoteRepositoryTest {

    private lateinit var repository: FakeNoteRepository

    @Before
    fun setUp() {
        repository = FakeNoteRepository(delayMs = 0L)
    }

    @Test
    fun getNoteVersionReturnsCurrentNoteWithStepsAndQuestions() = runTest {
        val result = repository.getNoteVersion("mc2")
        assertTrue(result.isSuccess)
        val note = (result as AppResult.Success).value
        assertEquals("mc2", note.machineId)
        assertEquals("v3", note.version)
        assertEquals(4, note.steps.size)
        assertEquals(3, note.questions.size)
        assertEquals("金型", note.questions[1].term)
    }

    @Test
    fun getVersionsReturnsHistory() = runTest {
        val result = repository.getVersions("mc2")
        assertTrue(result.isSuccess)
        val list = (result as AppResult.Success).value
        assertEquals(2, list.size)
        assertEquals("v3", list[0].version)
        assertEquals("v2", list[1].version)
    }

    @Test
    fun getVersionByIdReturnsSpecificVersion() = runTest {
        val result = repository.getVersion("nv_mc2_v2")
        assertTrue(result.isSuccess)
        val note = (result as AppResult.Success).value
        assertEquals("v2", note.version)
    }

    @Test
    fun triggerVersionUpdatePrependsNewVersion() = runTest {
        repository.triggerVersionUpdate("mc2", "v4")
        val current = repository.getNoteVersion("mc2")
        assertTrue(current.isSuccess)
        assertEquals("v4", (current as AppResult.Success).value.version)
    }
}
