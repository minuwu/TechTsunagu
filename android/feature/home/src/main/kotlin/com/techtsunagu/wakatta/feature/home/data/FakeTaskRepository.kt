package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.Tri
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTaskRepository(
    private val delayMs: Long = 200L
) : TaskRepository {

    @Inject
    constructor() : this(200L)

    private val tasks = mutableListOf(
        Task(
            id = "k1",
            title = mapOf(
                "ja" to "旋盤1号機の給油点検",
                "en" to "Lathe #1 oil inspection",
                "bn" to "লেদ ১ এর তেল পরীক্ষা"
            ),
            column = TaskColumn.TODO,
            assignees = listOf("u_rahim"),
            dueAt = "09:00",
            machineId = "mc1",
            version = 1
        ),
        Task(
            id = "k2",
            title = mapOf(
                "ja" to "2号プレスの金型清掃",
                "en" to "Press #2 die cleaning",
                "bn" to "প্রেস ২ ডাই পরিষ্কার"
            ),
            column = TaskColumn.DOING,
            assignees = listOf("u_rahim", "u_nguyen"),
            dueAt = "11:30",
            machineId = "mc2",
            version = 1
        ),
        Task(
            id = "k3",
            title = mapOf(
                "ja" to "製品パレットの運搬",
                "en" to "Transport product pallets",
                "bn" to "পণ্য প্যালেট পরিবহন"
            ),
            column = TaskColumn.DONE,
            assignees = listOf("u_siti"),
            dueAt = "16:00",
            machineId = "mc3",
            version = 2
        ),
        Task(
            id = "k4",
            title = mapOf(
                "ja" to "安全靴の点検",
                "en" to "Safety shoes check",
                "bn" to "নিরাপত্তা জুতা পরীক্ষা"
            ),
            column = TaskColumn.TODO,
            assignees = listOf("u_rahim"),
            dueAt = "14:00",
            version = 1
        )
    )

    override suspend fun getTasks(): AppResult<List<Task>> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(synchronized(tasks) { tasks.toList() })
    }

    override suspend fun getTask(id: String): AppResult<Task> {
        if (delayMs > 0) delay(delayMs)
        val task = synchronized(tasks) { tasks.find { it.id == id } }
        return if (task != null) {
            AppResult.Success(task)
        } else {
            AppResult.Failure(AppError.NotFound)
        }
    }

    override suspend fun createTask(
        title: Tri,
        assignees: List<String>,
        dueAt: String?
    ): AppResult<Task> {
        if (delayMs > 0) delay(delayMs)
        val newTask = Task(
            id = "k_${System.currentTimeMillis()}",
            title = title,
            column = TaskColumn.TODO,
            assignees = assignees,
            dueAt = dueAt,
            version = 1
        )
        synchronized(tasks) { tasks.add(newTask) }
        return AppResult.Success(newTask)
    }

    override suspend fun moveTask(
        id: String,
        column: TaskColumn,
        assignees: List<String>?,
        dueAt: String?,
        version: Int
    ): AppResult<Task> {
        if (delayMs > 0) delay(delayMs)
        synchronized(tasks) {
            val idx = tasks.indexOfFirst { it.id == id }
            if (idx == -1) return AppResult.Failure(AppError.NotFound)
            val current = tasks[idx]
            if (current.version != version) {
                return AppResult.Failure(AppError.Conflict("Task version conflict: expected ${current.version}, got $version"))
            }
            val updated = current.copy(
                column = column,
                assignees = assignees ?: current.assignees,
                dueAt = dueAt ?: current.dueAt,
                version = current.version + 1
            )
            tasks[idx] = updated
            return AppResult.Success(updated)
        }
    }
}
