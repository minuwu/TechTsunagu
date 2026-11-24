package com.techtsunagu.wakatta.feature.machines

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.ChatbotKey
import com.techtsunagu.wakatta.core.navigation.MachineKey
import com.techtsunagu.wakatta.core.navigation.MachinesKey
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.NoteKey
import com.techtsunagu.wakatta.core.navigation.PanelKey
import com.techtsunagu.wakatta.core.navigation.ScanKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MachinesEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: machines
        entry<MachinesKey> {
            MachinesScreen(
                onMachineClick = { id -> navigator.push(MachineKey(id)) },
                onTakeCheckClick = { id -> navigator.push(NoteKey(id)) },
            )
        }
        // Screen id: machine
        entry<MachineKey> { key ->
            MachineScreen(
                machineId = key.machineId,
                onBackClick = { navigator.pop() },
                onOpenPanelClick = { id -> navigator.push(PanelKey(id)) },
                onOpenNoteClick = { id -> navigator.push(NoteKey(id)) },
                onAskClick = { id -> navigator.push(ChatbotKey(id)) },
            )
        }
        // Screen id: scan
        entry<ScanKey> {
            ScanScreen(
                onBackClick = { navigator.pop() },
                onScanSuccess = { id -> navigator.push(PanelKey(id)) },
            )
        }
        // Screen id: panel
        entry<PanelKey> { key ->
            PanelScreen(
                machineId = key.machineId,
                onBackClick = { navigator.pop() },
                onOpenNoteClick = { id -> navigator.push(NoteKey(id)) },
            )
        }
        // Screen id: note
        entry<NoteKey> { key ->
            NoteScreen(
                machineId = key.machineId,
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MachinesModule {
    @Binds
    @IntoSet
    abstract fun bindMachinesEntries(entries: MachinesEntries): FeatureEntries
}
