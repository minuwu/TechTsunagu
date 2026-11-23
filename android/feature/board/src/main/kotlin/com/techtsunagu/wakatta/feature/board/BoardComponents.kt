package com.techtsunagu.wakatta.feature.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.TriText
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WakattaStatus

/**
 * 3-segment accessible control for moving tasks across To do, Doing, and Done.
 */
@Composable
fun TaskColumnSegmentedControl(
    selectedColumn: TaskColumn,
    onColumnSelected: (TaskColumn) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val columns = listOf(TaskColumn.TODO, TaskColumn.DOING, TaskColumn.DONE)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        columns.forEachIndexed { index, col ->
            val isSelected = selectedColumn == col
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                columns.lastIndex -> RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                else -> RoundedCornerShape(0.dp)
            }
            SegmentButton(
                column = col,
                isSelected = isSelected,
                shape = shape,
                enabled = enabled,
                onClick = { onColumnSelected(col) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SegmentButton(
    column: TaskColumn,
    isSelected: Boolean,
    shape: RoundedCornerShape,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (column) {
        TaskColumn.TODO -> stringResource(R.string.board_col_todo)
        TaskColumn.DOING -> stringResource(R.string.board_col_doing)
        TaskColumn.DONE -> stringResource(R.string.board_col_done)
        TaskColumn.UNKNOWN -> stringResource(R.string.board_col_todo)
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .height(48.dp)
            .semantics {
                role = SemanticsRole.RadioButton
                contentDescription = label
            },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 4.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

/**
 * Task card displayed in the Kanban column.
 */
@Composable
fun TaskCard(
    task: Task,
    lang: String,
    onClick: () -> Unit,
    onMoveClick: ((TaskColumn) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isDone = task.column == TaskColumn.DONE
    WakattaCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            TaskCardHeader(task = task, lang = lang, isDone = isDone)
            TaskCardMeta(task = task, lang = lang)
            TaskCardFooter(task = task, onMoveClick = onMoveClick)
        }
    }
}

@Composable
private fun TaskCardHeader(
    task: Task,
    lang: String,
    isDone: Boolean,
) {
    TriText(
        tri = task.title,
        lang = lang,
        style = MaterialTheme.typography.titleMedium.copy(
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
        ),
        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TaskCardMeta(
    task: Task,
    lang: String,
) {
    val machine = DemoSeed.machines.find { it.id == task.machineId }
    if (!task.dueAt.isNullOrEmpty() || machine != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            task.dueAt?.let { due ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = stringResource(R.string.board_task_due),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = due,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            machine?.let { m ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = stringResource(R.string.board_task_machine),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    TriText(
                        tri = m.name,
                        lang = lang,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCardFooter(
    task: Task,
    onMoveClick: ((TaskColumn) -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy((-6).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            task.assignees.forEach { userId ->
                val user = DemoSeed.users.find { it.id == userId }
                val displayName = user?.displayName ?: userId
                Avatar(
                    name = displayName,
                    size = 28.dp,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        if (task.fromMessageId != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(R.string.board_task_from_chat),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        if (onMoveClick != null) {
            QuickMoveControls(column = task.column, onMoveClick = onMoveClick)
        }
    }
}

@Composable
private fun QuickMoveControls(
    column: TaskColumn,
    onMoveClick: (TaskColumn) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        when (column) {
            TaskColumn.TODO -> {
                OutlinedButton(
                    onClick = { onMoveClick(TaskColumn.DOING) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.sizeIn(minHeight = 32.dp),
                ) {
                    Text(text = stringResource(R.string.board_col_doing), fontSize = 11.sp)
                }
            }
            TaskColumn.DOING -> {
                OutlinedButton(
                    onClick = { onMoveClick(TaskColumn.DONE) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.sizeIn(minHeight = 32.dp),
                ) {
                    Text(text = stringResource(R.string.board_col_done), fontSize = 11.sp)
                }
            }
            TaskColumn.DONE -> {
                OutlinedButton(
                    onClick = { onMoveClick(TaskColumn.TODO) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.sizeIn(minHeight = 32.dp),
                ) {
                    Text(text = stringResource(R.string.board_col_todo), fontSize = 11.sp)
                }
            }
            TaskColumn.UNKNOWN -> Unit
        }
    }
}

/**
 * Kanban column with header dot, count, and scrollable task list.
 */
@Composable
fun KanbanColumnPage(
    column: TaskColumn,
    tasks: List<Task>,
    lang: String,
    onTaskClick: (String) -> Unit,
    onMoveTask: (String, TaskColumn) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (titleRes, emptyRes, dotColor) = when (column) {
        TaskColumn.TODO -> Triple(
            R.string.board_col_todo,
            R.string.board_empty_todo,
            MaterialTheme.colorScheme.outline
        )
        TaskColumn.DOING -> Triple(
            R.string.board_col_doing,
            R.string.board_empty_doing,
            StatusColors.forStatus(WakattaStatus.ORANGE).color
        )
        TaskColumn.DONE -> Triple(
            R.string.board_col_done,
            R.string.board_empty_done,
            StatusColors.forStatus(WakattaStatus.GREEN).color
        )
        TaskColumn.UNKNOWN -> Triple(
            R.string.board_col_todo,
            R.string.board_empty,
            MaterialTheme.colorScheme.outline
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
    ) {
        ColumnHeader(
            title = stringResource(titleRes),
            count = tasks.size,
            dotColor = dotColor,
        )
        if (tasks.isEmpty()) {
            EmptyState(
                message = stringResource(emptyRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                items(
                    items = tasks,
                    key = { it.id },
                    contentType = { "task_card" },
                ) { task ->
                    TaskCard(
                        task = task,
                        lang = lang,
                        onClick = { onTaskClick(task.id) },
                        onMoveClick = { targetCol -> onMoveTask(task.id, targetCol) },
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnHeader(
    title: String,
    count: Int,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(dotColor, CircleShape),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * BottomSheet for creating a new task.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onSubmit: (title: String, assignees: List<String>, dueAt: String?, machineId: String?) -> Unit,
    users: List<User> = DemoSeed.users,
    machines: List<Machine> = DemoSeed.machines,
) {
    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    val selectedAssignees = remember { mutableStateListOf<String>() }
    var selectedDue by remember { mutableStateOf<String?>("Today") }
    var selectedMachine by remember { mutableStateOf<String?>(null) }

    val dues = listOf(
        "Today" to R.string.board_due_today,
        "Tomorrow" to R.string.board_due_tomorrow,
        "Fri" to R.string.board_due_fri
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.board_add_task),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (titleError && it.isNotBlank()) titleError = false
                },
                label = { Text(stringResource(R.string.board_add_task_title)) },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text(stringResource(R.string.board_add_task_title_required)) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.board_add_task_assignees),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                users.forEach { user ->
                    val isSelected = selectedAssignees.contains(user.id)
                    WakattaChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedAssignees.remove(user.id)
                            else selectedAssignees.add(user.id)
                        },
                        label = { Text(user.displayName) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.board_add_task_due),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                dues.forEach { (value, labelRes) ->
                    val isSelected = selectedDue == value
                    WakattaChip(
                        selected = isSelected,
                        onClick = { selectedDue = if (isSelected) null else value },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.board_add_task_machine),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                machines.forEach { machine ->
                    val isSelected = selectedMachine == machine.id
                    val name = machine.name["en"] ?: machine.code
                    WakattaChip(
                        selected = isSelected,
                        onClick = { selectedMachine = if (isSelected) null else machine.id },
                        label = { Text(name) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSubmit(
                            title.trim(),
                            selectedAssignees.toList(),
                            selectedDue,
                            selectedMachine
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(
                    text = stringResource(R.string.board_add_task_submit),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
