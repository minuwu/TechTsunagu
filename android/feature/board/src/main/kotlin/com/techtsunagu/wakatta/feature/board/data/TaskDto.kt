package com.techtsunagu.wakatta.feature.board.data

import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.Tri
import kotlinx.serialization.Serializable

/**
 * Task DTO matching docs/MOBILE_API_CONTRACT.md section 5.1 (E-060, E-061, E-062).
 */
@Serializable
data class TaskDto(
    val id: String,
    val title: Tri,
    val column: String,
    val assigneeIds: List<String> = emptyList(),
    val dueAt: String? = null,
    val machineId: String? = null,
    val fromMessageId: String? = null,
    val version: Int = 1,
    val updatedAt: String? = null,
)

fun TaskDto.toDomain(): Task = Task(
    id = id,
    title = title,
    column = when (column.lowercase()) {
        "doing" -> TaskColumn.DOING
        "done" -> TaskColumn.DONE
        else -> TaskColumn.TODO
    },
    assignees = assigneeIds,
    dueAt = dueAt,
    machineId = machineId,
    fromMessageId = fromMessageId,
    version = version,
)

fun Task.toDto(): TaskDto = TaskDto(
    id = id,
    title = title,
    column = when (column) {
        TaskColumn.TODO -> "todo"
        TaskColumn.DOING -> "doing"
        TaskColumn.DONE -> "done"
        TaskColumn.UNKNOWN -> "todo"
    },
    assigneeIds = assignees,
    dueAt = dueAt,
    machineId = machineId,
    fromMessageId = fromMessageId,
    version = version,
)

@Serializable
data class TaskCreateRequest(
    val title: Tri,
    val assigneeIds: List<String> = emptyList(),
    val dueAt: String? = null,
    val machineId: String? = null,
)

@Serializable
data class TaskUpdateRequest(
    val column: String? = null,
    val assigneeIds: List<String>? = null,
    val dueAt: String? = null,
)
