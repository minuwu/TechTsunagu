package com.techtsunagu.wakatta.feature.board.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.Tri
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory Fake implementation of TaskRepository.
 * Features 250ms simulated latency and HTTP 412 PreconditionFailed stale-edit conflict detection.
 */
@Singleton
class FakeTaskRepository(
    private val delayMs: Long = 250L,
) : TaskRepository, TaskCreator {

    @Inject
    constructor() : this(250L)

    private val mutex = Mutex()
    private var simulatedError: AppError? = null

    // Default seeded tasks ported from prototypes/wakatta/parts/skeleton.html DATA.tasks
    private val tasks = mutableListOf(
        Task(
            id = "k1",
            title = mapOf(
                "ja" to "3号機 オイル点検",
                "en" to "Check oil · Machine 3",
                "bn" to "৩ নং মেশিনের তেল পরীক্ষা"
            ),
            column = TaskColumn.DONE,
            assignees = listOf("u_siti"),
            dueAt = "Today 09:00",
            machineId = "mc3",
            fromMessageId = "m1",
            version = 1,
        ),
        Task(
            id = "k2",
            title = mapOf(
                "ja" to "旋盤1 切粉トレー清掃",
                "en" to "Clean chip tray · Lathe 1",
                "bn" to "লেদ ১ এর চিপ ট্রে পরিষ্কার"
            ),
            column = TaskColumn.DOING,
            assignees = listOf("u_nguyen"),
            dueAt = "Today 12:00",
            machineId = "mc1",
            fromMessageId = null,
            version = 1,
        ),
        Task(
            id = "k3",
            title = mapOf(
                "ja" to "手袋 Lサイズ 発注",
                "en" to "Order gloves (size L)",
                "bn" to "দস্তানা অর্ডার (সাইজ L)"
            ),
            column = TaskColumn.TODO,
            assignees = listOf("u_tanaka"),
            dueAt = "Fri",
            machineId = null,
            fromMessageId = null,
            version = 1,
        ),
        Task(
            id = "k4",
            title = mapOf(
                "ja" to "2号プレス 金型交換",
                "en" to "Die change · Press #2",
                "bn" to "২ নং প্রেসের ছাঁচ পরিবর্তন"
            ),
            column = TaskColumn.TODO,
            assignees = listOf("u_rahim", "u_nguyen"),
            dueAt = "Tomorrow 08:00",
            machineId = "mc2",
            fromMessageId = null,
            version = 1,
        ),
        Task(
            id = "k5",
            title = mapOf(
                "ja" to "交換前 ロックアウト・保護具",
                "en" to "Lockout + PPE before change",
                "bn" to "পরিবর্তনের আগে লকআউট ও পিপিই"
            ),
            column = TaskColumn.TODO,
            assignees = listOf("u_rahim", "u_nguyen"),
            dueAt = "Tomorrow 07:45",
            machineId = "mc2",
            fromMessageId = null,
            version = 1,
        )
    )

    fun simulateError(error: AppError?) {
        simulatedError = error
    }

    fun resetToDefault() {
        tasks.clear()
        tasks.addAll(
            listOf(
                Task(
                    id = "k1",
                    title = mapOf(
                        "ja" to "3号機 オイル点検",
                        "en" to "Check oil · Machine 3",
                        "bn" to "৩ নং মেশিনের তেল পরীক্ষা"
                    ),
                    column = TaskColumn.DONE,
                    assignees = listOf("u_siti"),
                    dueAt = "Today 09:00",
                    machineId = "mc3",
                    fromMessageId = "m1",
                    version = 1,
                ),
                Task(
                    id = "k2",
                    title = mapOf(
                        "ja" to "旋盤1 切粉トレー清掃",
                        "en" to "Clean chip tray · Lathe 1",
                        "bn" to "লেদ ১ এর চিপ ট্রে পরিষ্কার"
                    ),
                    column = TaskColumn.DOING,
                    assignees = listOf("u_nguyen"),
                    dueAt = "Today 12:00",
                    machineId = "mc1",
                    fromMessageId = null,
                    version = 1,
                ),
                Task(
                    id = "k3",
                    title = mapOf(
                        "ja" to "手袋 Lサイズ 発注",
                        "en" to "Order gloves (size L)",
                        "bn" to "দস্তানা অর্ডার (সাইজ L)"
                    ),
                    column = TaskColumn.TODO,
                    assignees = listOf("u_tanaka"),
                    dueAt = "Fri",
                    machineId = null,
                    fromMessageId = null,
                    version = 1,
                ),
                Task(
                    id = "k4",
                    title = mapOf(
                        "ja" to "2号プレス 金型交換",
                        "en" to "Die change · Press #2",
                        "bn" to "২ নং প্রেসের ছাঁচ পরিবর্তন"
                    ),
                    column = TaskColumn.TODO,
                    assignees = listOf("u_rahim", "u_nguyen"),
                    dueAt = "Tomorrow 08:00",
                    machineId = "mc2",
                    fromMessageId = null,
                    version = 1,
                ),
                Task(
                    id = "k5",
                    title = mapOf(
                        "ja" to "交換前 ロックアウト・保護具",
                        "en" to "Lockout + PPE before change",
                        "bn" to "পরিবর্তনের আগে লকআউট ও পিপিই"
                    ),
                    column = TaskColumn.TODO,
                    assignees = listOf("u_rahim", "u_nguyen"),
                    dueAt = "Tomorrow 07:45",
                    machineId = "mc2",
                    fromMessageId = null,
                    version = 1,
                )
            )
        )
    }

    override suspend fun getTasks(): AppResult<List<Task>> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            val err = simulatedError
            if (err != null) {
                return@withLock AppResult.Failure(err)
            }
            AppResult.Success(tasks.toList())
        }
    }

    override suspend fun getTask(id: String): AppResult<Task> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            val err = simulatedError
            if (err != null) {
                return@withLock AppResult.Failure(err)
            }
            val task = tasks.find { it.id == id }
            if (task != null) {
                AppResult.Success(task)
            } else {
                AppResult.Failure(AppError.NotFound)
            }
        }
    }

    override suspend fun createTask(
        title: Tri,
        assignees: List<String>,
        dueAt: String?
    ): AppResult<Task> = createTask(title, assignees, dueAt, machineId = null)

    override suspend fun createTask(
        title: Tri,
        assignees: List<String>,
        dueAt: String?,
        machineId: String?
    ): AppResult<Task> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            val err = simulatedError
            if (err != null) {
                return@withLock AppResult.Failure(err)
            }
            val newId = "k" + (tasks.size + 1) + "_" + System.currentTimeMillis().toString(36)
            val newTask = Task(
                id = newId,
                title = title,
                column = TaskColumn.TODO,
                assignees = assignees,
                dueAt = dueAt,
                machineId = machineId,
                fromMessageId = null,
                version = 1,
            )
            tasks.add(newTask)
            AppResult.Success(newTask)
        }
    }

    override suspend fun moveTask(
        id: String,
        column: TaskColumn,
        assignees: List<String>?,
        dueAt: String?,
        version: Int
    ): AppResult<Task> {
        if (delayMs > 0) delay(delayMs)
        return mutex.withLock {
            val err = simulatedError
            if (err != null) {
                return@withLock AppResult.Failure(err)
            }
            val index = tasks.indexOfFirst { it.id == id }
            if (index == -1) {
                return@withLock AppResult.Failure(AppError.NotFound)
            }
            val current = tasks[index]
            if (version != current.version) {
                // Stale-edit conflict: HTTP 412 PreconditionFailed
                return@withLock AppResult.Failure(AppError.PreconditionFailed)
            }
            val updated = current.copy(
                column = column,
                assignees = assignees ?: current.assignees,
                dueAt = dueAt ?: current.dueAt,
                version = current.version + 1,
            )
            tasks[index] = updated
            AppResult.Success(updated)
        }
    }
}
