package com.techtsunagu.wakatta.feature.machines

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.CheckResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineDetail
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Passport
import com.techtsunagu.wakatta.core.model.QrScanner
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SignOff
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.Step
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TestSessionStore(
    initialSession: Session? = Session(
        token = "test-worker-token",
        user = DemoSeed.demoWorker
    )
) : SessionStore {
    private val _session = MutableStateFlow<Session?>(initialSession)
    override val session: StateFlow<Session?> = _session

    fun setForemanSession() {
        _session.value = Session(
            token = "test-foreman-token",
            user = DemoSeed.demoForeman
        )
    }

    fun setWorkerSession(user: User = DemoSeed.demoWorker) {
        _session.value = Session(
            token = "test-worker-token",
            user = user
        )
    }

    fun clearSession() {
        _session.value = null
    }

    override suspend fun save(session: Session) {
        _session.value = session
    }

    override suspend fun clear() {
        _session.value = null
    }

    override fun getCachedToken(): String? = _session.value?.token
}

class TestMachineRepository : MachineRepository {
    var shouldFail: Boolean = false
    var machinesList: List<Machine> = DemoSeed.machines

    override suspend fun getMachines(): AppResult<List<Machine>> {
        if (shouldFail) return AppResult.Failure(AppError.Server)
        return AppResult.Success(machinesList)
    }

    override suspend fun getMachine(id: String): AppResult<MachineDetail> {
        if (shouldFail) return AppResult.Failure(AppError.NotFound)
        val m = machinesList.find { it.id == id } ?: DemoSeed.machines.find { it.id == id }
        if (m == null) return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(
            MachineDetail(
                id = m.id,
                code = m.code,
                name = m.name,
                currentVersionId = m.currentVersionId ?: "v1",
                steps = listOf(
                    Step(text = mapOf("ja" to "主電源をON", "en" to "Turn main power ON", "bn" to "মেইন পাওয়ার চালু করুন")),
                    Step(text = mapOf("ja" to "安全カバーを確認", "en" to "Check safety cover", "bn" to "নিরাপত্তা কভার পরীক্ষা করুন"))
                )
            )
        )
    }

    override suspend fun scan(machineId: String): AppResult<Unit> {
        if (shouldFail) return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(Unit)
    }
}

class TestSignOffRepository : SignOffRepository {
    var shouldFail: Boolean = false
    var signOffsList: List<SignOff> = listOf(
        SignOff(
            machineId = "mc1",
            machineName = mapOf("ja" to "旋盤1号機"),
            passedAt = "2026-10-01T08:00:00Z",
            expiresAt = "2026-12-30T08:00:00Z",
            status = SkillStatus.VALID
        ),
        SignOff(
            machineId = "mc3",
            machineName = mapOf("ja" to "3号機 成形"),
            passedAt = "2026-09-01T08:00:00Z",
            expiresAt = "2026-11-30T08:00:00Z",
            status = SkillStatus.VALID
        )
    )

    override suspend fun getSignOffs(workerId: String): AppResult<List<SignOff>> {
        if (shouldFail) return AppResult.Failure(AppError.Server)
        return AppResult.Success(signOffsList)
    }

    override suspend fun getPassport(workerId: String): AppResult<Passport> {
        return AppResult.Success(
            Passport(
                workerId = workerId,
                workerName = "Rahim",
                signOffs = signOffsList
            )
        )
    }
}

class TestCheckRepository : CheckRepository {
    var outcome: CheckResult = CheckResult(passed = true, signOffGranted = true)
    var shouldFailWithConflict: Boolean = false
    var shouldFail: Boolean = false
    var submittedTargetId: String? = null
    var submittedAnswers: List<Int>? = null
    val submittedKeys = mutableListOf<String>()

    override suspend fun submitCheck(
        targetKind: CheckTargetKind,
        targetId: String,
        language: String,
        answers: List<Int>,
        idempotencyKey: String
    ): AppResult<CheckResult> {
        submittedTargetId = targetId
        submittedAnswers = answers
        submittedKeys += idempotencyKey
        if (shouldFailWithConflict) {
            return AppResult.Failure(AppError.Conflict("VERSION_CHANGED"))
        }
        if (shouldFail) {
            return AppResult.Failure(AppError.Server)
        }
        return AppResult.Success(outcome)
    }
}

class TestSpeaker : Speaker {
    val spokenUtterances = mutableListOf<Pair<String, String>>()
    var isCurrentlySpeaking = false

    override fun speak(text: String, languageTag: String) {
        spokenUtterances.add(text to languageTag)
        isCurrentlySpeaking = true
    }

    override fun isAvailable(languageTag: String): Boolean = true

    override fun stop() {
        isCurrentlySpeaking = false
    }
}

class TestQrScanner : QrScanner {
    val resultsFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override fun scanResults(): Flow<String> = resultsFlow
}

class TestWorkerRepository : WorkerRepository {
    override suspend fun getWorkers(): AppResult<List<WorkerInfo>> = AppResult.Success(
        listOf(
            WorkerInfo("u_rahim", "Rahim Uddin", "bn"),
            WorkerInfo("u_nguyen", "Nguyen Van An", "vi"),
            WorkerInfo("u_siti", "Siti Rahma", "id"),
        ),
    )
}

class TestTaskRepository : TaskRepository {
    val tasks = mutableListOf(
        Task(id = "t1", title = mapOf("ja" to "作業前点検", "en" to "Pre-work check"), column = TaskColumn.TODO, machineId = "mc1", dueAt = "08:30"),
        Task(id = "t2", title = mapOf("en" to "Other machine"), column = TaskColumn.TODO, machineId = "mc2"),
    )

    override suspend fun getTasks(): AppResult<List<Task>> = AppResult.Success(tasks)
    override suspend fun getTask(id: String): AppResult<Task> = AppResult.Failure(AppError.NotFound)
    override suspend fun createTask(title: Tri, assignees: List<String>, dueAt: String?): AppResult<Task> =
        AppResult.Failure(AppError.NotFound)
    override suspend fun moveTask(id: String, column: TaskColumn, assignees: List<String>?, dueAt: String?, version: Int): AppResult<Task> =
        AppResult.Failure(AppError.NotFound)
}
