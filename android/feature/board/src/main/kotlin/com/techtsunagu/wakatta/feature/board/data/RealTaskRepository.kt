package com.techtsunagu.wakatta.feature.board.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Create with a Machine: the Task Board sheet picks one, the 3-argument `TaskRepository.createTask` cannot carry it. */
interface TaskCreator {
    suspend fun createTask(title: Tri, assignees: List<String>, dueAt: String?, machineId: String?): AppResult<Task>
}

/**
 * Task Board over `GET|POST /tasks` and `PATCH /tasks/{id}` (`If-Match`; 412 comes back as
 * `AppError.PreconditionFailed`, the screens reload on it).
 */
internal class RealTaskRepository(
    private val api: () -> TaskApi,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : TaskRepository, TaskCreator {

    // One key per user action: an identical create that failed is retried with the same key until it succeeds.
    private val keys = ConcurrentHashMap<Int, String>()

    override suspend fun getTasks(): AppResult<List<Task>> {
        val all = mutableListOf<TaskDto>()
        var cursor: String? = null
        do {
            val page = when (val r = safeApiCall { api().tasks(PAGE_LIMIT, cursor) }) {
                is AppResult.Success -> r.value
                is AppResult.Failure -> return r
            }
            all += page.items
            cursor = page.nextCursor
        } while (cursor != null)
        return AppResult.Success(all.map { it.toTask() })
    }

    // shortcut: there is no `GET /tasks/{id}`, so a card is found in the list, wire when the backend ships it.
    override suspend fun getTask(id: String): AppResult<Task> =
        when (val r = getTasks()) {
            is AppResult.Success -> r.value.firstOrNull { it.id == id }?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> r
        }

    override suspend fun createTask(title: Tri, assignees: List<String>, dueAt: String?): AppResult<Task> =
        createTask(title, assignees, dueAt, machineId = null)

    override suspend fun createTask(
        title: Tri,
        assignees: List<String>,
        dueAt: String?,
        machineId: String?,
    ): AppResult<Task> {
        val body = TaskCreateRequest(title, assignees, dueWire(dueAt, ZonedDateTime.now(zone)), machineId)
        val signature = body.hashCode()
        val key = keys.getOrPut(signature) { UUID.randomUUID().toString() }
        val result = safeApiCall { api().create(IdempotencyKey(key), body) }
        if (result.isSuccess) keys.remove(signature)
        return result.map { it.toTask() }
    }

    /** shortcut: the due time and the Machine cannot be changed from the app, so only the column and the assignees go out. */
    override suspend fun moveTask(
        id: String,
        column: TaskColumn,
        assignees: List<String>?,
        dueAt: String?,
        version: Int,
    ): AppResult<Task> {
        val body = TaskUpdateRequest(column = column.name.lowercase(), assigneeIds = assignees)
        return safeApiCall { api().update(id, version.toString(), body) }.map { it.toTask() }
    }

    private fun TaskDto.toTask(): Task = toDomain().copy(dueAt = dueDisplay(dueAt, zone))

    private fun <A, B> AppResult<A>.map(transform: (A) -> B): AppResult<B> = when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Failure -> this
    }

    private companion object {
        const val PAGE_LIMIT = 200
    }
}

private val dueFormat = DateTimeFormatter.ofPattern("M/d HH:mm")
private const val END_OF_DAY_HOUR = 18

/** The card shows `M/d HH:mm` in the phone's zone; anything that is not a date-time is shown as it came. */
internal fun dueDisplay(iso: String?, zone: ZoneId): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(iso).atZoneSameInstant(zone).format(dueFormat) }.getOrDefault(iso)
}

/**
 * The Add Task sheet offers Today, Tomorrow and Fri; the server wants a date-time. shortcut: a label means 18:00 of
 * that day (the next Friday for "Fri"), wire a time picker when the sheet gets one. A real date-time passes through.
 */
internal fun dueWire(label: String?, now: ZonedDateTime): String? {
    val day = when (label) {
        null, "" -> return null
        "Today" -> now.toLocalDate()
        "Tomorrow" -> now.toLocalDate().plusDays(1)
        "Fri" -> now.toLocalDate().plusDays(((5 - now.dayOfWeek.value + 7) % 7).toLong())
        else -> return label.takeIf { runCatching { OffsetDateTime.parse(it) }.isSuccess }
    }
    return day.atTime(END_OF_DAY_HOUR, 0).atZone(now.zone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}
