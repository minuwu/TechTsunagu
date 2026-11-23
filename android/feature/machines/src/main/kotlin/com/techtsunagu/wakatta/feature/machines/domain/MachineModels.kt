package com.techtsunagu.wakatta.feature.machines.domain

import com.techtsunagu.wakatta.core.model.NoteStatus
import com.techtsunagu.wakatta.core.model.PanelState
import com.techtsunagu.wakatta.core.model.Shift
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.Tri

data class PanelStateDto(
    val machineId: String,
    val machineName: Tri,
    val state: PanelState,
    val activeWorkerId: String? = null,
    val activeWorkerName: String? = null,
    val override: PanelOverrideDto? = null,
    val workerStatuses: List<PanelWorkerStatus> = emptyList(),
)

data class PanelWorkerStatus(
    val workerId: String,
    val displayName: String,
    val isReady: Boolean,
    val hasSignOff: Boolean,
    val signOffVersion: String? = null,
    val signOffAt: String? = null,
    val isPresent: Boolean = false,
)

data class PanelOverrideDto(
    val id: String,
    val machineId: String,
    val workerId: String? = null,
    val by: String,
    val reason: String,
    val minutes: Int,
    val startsAt: String,
    val endsAt: String,
    val revokedAt: String? = null,
)

data class PanelStateResult(
    val panel: PanelStateDto,
    val etag: String,
    val isNotModified: Boolean = false,
)

data class ShiftAssignmentDto(
    val id: String,
    val machineId: String,
    val workerId: String? = null,
    val workerName: String? = null,
    val shift: Shift = Shift.DAY,
    val date: String,
)

data class NoteVersionDto(
    val id: String,
    val machineId: String,
    val version: String,
    val status: NoteStatus = NoteStatus.APPROVED,
    val updatedAt: String,
    val by: String,
    val steps: List<NoteStepDto> = emptyList(),
    val questions: List<NoteQuestionDto> = emptyList(),
    val needsEdit: Boolean = false,
)

data class NoteStepDto(
    val index: Int,
    val icon: String,
    val textJa: String,
    val textEn: String,
    val textBn: String,
    val backJa: String,
)

data class NoteQuestionDto(
    val id: String,
    val stepIndex: Int,
    val term: String,
    val qJa: String,
    val qEn: String,
    val qBn: String,
    val optionsJa: List<String>,
    val optionsEn: List<String>,
    val optionsBn: List<String>,
    /** The answer key: only a Foreman's Note Version carries it. A Worker's is always null (rule R-3). */
    val correctIndex: Int? = null,
)

data class MachineTaskSummary(
    val id: String,
    val title: Tri,
    val dueText: String,
)
