package com.techtsunagu.wakatta.feature.board

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.BoardKey
import com.techtsunagu.wakatta.core.navigation.ChatKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.MachineKey
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.TaskKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoardEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: board
        entry<BoardKey> {
            BoardScreen(
                onTaskClick = { taskId -> navigator.push(TaskKey(taskId)) },
            )
        }
        // Screen id: task
        entry<TaskKey> { key ->
            TaskDetailScreen(
                taskId = key.taskId,
                onBackClick = { navigator.pop() },
                onNavigateToMachine = { machineId -> navigator.push(MachineKey(machineId)) },
                onNavigateToChat = { threadId -> navigator.push(ChatKey(threadId)) },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BoardModule {
    @Binds
    @IntoSet
    abstract fun bindBoardEntries(entries: BoardEntries): FeatureEntries
}
