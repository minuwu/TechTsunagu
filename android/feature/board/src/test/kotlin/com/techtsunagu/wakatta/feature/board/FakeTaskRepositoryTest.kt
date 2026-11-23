package com.techtsunagu.wakatta.feature.board

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.feature.board.data.FakeTaskRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeTaskRepositoryTest {

    private lateinit var repository: FakeTaskRepository

    @Before
    fun setUp() {
        repository = FakeTaskRepository(delayMs = 0L)
    }

    @Test
    fun getTasksReturnsSeededTasks() = runTest {
        val result = repository.getTasks()
        assertTrue(result.isSuccess)
        val tasks = result.getOrNull()
        assertNotNull(tasks)
        assertEquals(5, tasks!!.size)
    }

    @Test
    fun getTaskReturnsSpecificTask() = runTest {
        val result = repository.getTask("k1")
        assertTrue(result.isSuccess)
        val task = result.getOrNull()
        assertNotNull(task)
        assertEquals("k1", task!!.id)
        assertEquals(TaskColumn.DONE, task.column)
    }

    @Test
    fun getTaskNonExistentReturnsNotFound() = runTest {
        val result = repository.getTask("non_existent_id")
        assertTrue(result.isFailure)
        assertEquals(AppError.NotFound, result.errorOrNull())
    }

    @Test
    fun createTaskAppendsTaskWithVersionOne() = runTest {
        val title = mapOf("ja" to "新規タスク", "en" to "New Task", "bn" to "নতুন কাজ")
        val result = repository.createTask(
            title = title,
            assignees = listOf("u_rahim"),
            dueAt = "Today 15:00",
            machineId = "mc1"
        )
        assertTrue(result.isSuccess)
        val created = result.getOrNull()
        assertNotNull(created)
        assertEquals(1, created!!.version)
        assertEquals(TaskColumn.TODO, created.column)
        assertEquals(listOf("u_rahim"), created.assignees)

        val all = repository.getTasks().getOrNull()
        assertEquals(6, all?.size)
    }

    @Test
    fun moveTaskUpdatesColumnAndIncrementsVersion() = runTest {
        val initial = repository.getTask("k3").getOrNull()!!
        assertEquals(TaskColumn.TODO, initial.column)
        assertEquals(1, initial.version)

        val result = repository.moveTask(
            id = "k3",
            column = TaskColumn.DOING,
            assignees = null,
            dueAt = null,
            version = 1,
        )
        assertTrue(result.isSuccess)
        val updated = result.getOrNull()!!
        assertEquals(TaskColumn.DOING, updated.column)
        assertEquals(2, updated.version)
    }

    @Test
    fun moveTaskWithWrongVersionFailsWithPreconditionFailedHttp412() = runTest {
        val initial = repository.getTask("k2").getOrNull()!!
        assertEquals(1, initial.version)

        // Simulate stale edit: attempting to move with version = 99
        val result = repository.moveTask(
            id = "k2",
            column = TaskColumn.DONE,
            assignees = null,
            dueAt = null,
            version = 99,
        )
        assertTrue(result.isFailure)
        assertEquals(AppError.PreconditionFailed, result.errorOrNull())
    }

    @Test
    fun moveTaskNonExistentReturnsNotFound() = runTest {
        val result = repository.moveTask(
            id = "missing",
            column = TaskColumn.DONE,
            assignees = null,
            dueAt = null,
            version = 1,
        )
        assertTrue(result.isFailure)
        assertEquals(AppError.NotFound, result.errorOrNull())
    }

    @Test
    fun simulatedErrorReturnsFailure() = runTest {
        repository.simulateError(AppError.Offline)
        val result = repository.getTasks()
        assertTrue(result.isFailure)
        assertEquals(AppError.Offline, result.errorOrNull())

        // Next call succeeds when error cleared
        repository.simulateError(null)
        val next = repository.getTasks()
        assertTrue(next.isSuccess)
    }

    @Test
    fun resetToDefaultRestoresSeed() = runTest {
        repository.createTask(mapOf("en" to "Extra"), listOf("u_tanaka"), null)
        assertEquals(6, repository.getTasks().getOrNull()?.size)

        repository.resetToDefault()
        assertEquals(5, repository.getTasks().getOrNull()?.size)
    }
}
