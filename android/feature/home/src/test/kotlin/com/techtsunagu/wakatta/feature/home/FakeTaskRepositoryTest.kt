package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.TaskColumn
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
    fun getTasksReturnsInitialList() = runTest {
        val result = repository.getTasks()
        assertTrue(result is AppResult.Success)
        val tasks = (result as AppResult.Success).value
        assertTrue(tasks.size >= 4)
    }

    @Test
    fun getTaskFindsExistingTask() = runTest {
        val result = repository.getTask("k1")
        assertTrue(result is AppResult.Success)
        val task = (result as AppResult.Success).value
        assertEquals("k1", task.id)
    }

    @Test
    fun getTaskReturnsNotFoundForInvalidId() = runTest {
        val result = repository.getTask("non_existent")
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.NotFound)
    }

    @Test
    fun createTaskAppendsNewTask() = runTest {
        val result = repository.createTask(
            title = mapOf("en" to "New Task"),
            assignees = listOf("u_rahim"),
            dueAt = "15:00",
        )
        assertTrue(result is AppResult.Success)
        val created = (result as AppResult.Success).value
        assertNotNull(created.id)
        assertEquals(TaskColumn.TODO, created.column)

        val listResult = repository.getTasks()
        val allTasks = (listResult as AppResult.Success).value
        assertTrue(allTasks.any { it.id == created.id })
    }

    @Test
    fun moveTaskUpdatesColumnAndIncrementsVersion() = runTest {
        val result = repository.moveTask(
            id = "k1",
            column = TaskColumn.DONE,
            assignees = null,
            dueAt = null,
            version = 1,
        )
        assertTrue(result is AppResult.Success)
        val updated = (result as AppResult.Success).value
        assertEquals(TaskColumn.DONE, updated.column)
        assertEquals(2, updated.version)
    }

    @Test
    fun moveTaskConflictOnStaleVersion() = runTest {
        val result = repository.moveTask(
            id = "k1",
            column = TaskColumn.DONE,
            assignees = null,
            dueAt = null,
            version = 999,
        )
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.Conflict)
    }
}
