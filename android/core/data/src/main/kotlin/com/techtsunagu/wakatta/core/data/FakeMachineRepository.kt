package com.techtsunagu.wakatta.core.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckQuestion
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineDetail
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Step
import com.techtsunagu.wakatta.core.model.Tri
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeMachineRepository(
    private val delayMs: Long = 200L
) : MachineRepository {

    @Inject
    constructor() : this(200L)


    private val machines = DemoSeed.machines.toMutableList()

    override suspend fun getMachines(): AppResult<List<Machine>> {
        if (delayMs > 0) delay(delayMs)
        return AppResult.Success(machines.toList())
    }

    override suspend fun addMachine(code: String, name: Tri): AppResult<Machine> {
        if (machines.any { it.code == code }) return AppResult.Failure(AppError.Conflict("CONFLICT"))
        val m = Machine(id = "m-$code", code = code, name = name)
        machines += m
        return AppResult.Success(m)
    }

    override suspend fun renameMachine(id: String, name: Tri): AppResult<Machine> {
        val i = machines.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return AppResult.Failure(AppError.NotFound)
        machines[i] = machines[i].copy(name = name)
        return AppResult.Success(machines[i])
    }

    override suspend fun archiveMachine(id: String): AppResult<Unit> {
        machines.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }

    override suspend fun getMachine(id: String): AppResult<MachineDetail> {
        if (delayMs > 0) delay(delayMs)
        val machine = machines.find { it.id == id } ?: return AppResult.Failure(AppError.NotFound)

        // Return machine detail with steps and questions (Worker responses never contain correctIndex)
        val detail = MachineDetail(
            id = machine.id,
            code = machine.code,
            name = machine.name,
            currentVersionId = machine.currentVersionId,
            steps = listOf(
                Step(
                    text = mapOf(
                        "ja" to "作業前に安全カバーを点検する",
                        "en" to "Inspect safety cover before work",
                        "bn" to "কাজের আগে নিরাপত্তা কভার পরীক্ষা করুন"
                    ),
                    icon = "safety"
                ),
                Step(
                    text = mapOf(
                        "ja" to "主電源を入れて異常音がないか確認する",
                        "en" to "Turn on main switch and check for abnormal sound",
                        "bn" to "প্রধান সুইচ চালু করুন এবং কোনো অস্বাভাবিক শব্দ আছে কিনা দেখুন"
                    ),
                    icon = "power"
                ),
                Step(
                    text = mapOf(
                        "ja" to "非常停止ボタンを押して即座に停止するか試す",
                        "en" to "Test emergency stop button for immediate halt",
                        "bn" to "জরুরি বোতাম টিপে তাৎক্ষণিক থামানো পরীক্ষা করুন"
                    ),
                    icon = "stop"
                )
            ),
            questions = listOf(
                CheckQuestion(
                    id = "q_${machine.id}_1",
                    termId = null,
                    prompt = mapOf(
                        "ja" to "作業開始前に点検するものは何ですか？",
                        "en" to "What must you check before starting?",
                        "bn" to "শুরু করার আগে কী পরীক্ষা করতে হবে?"
                    ),
                    options = listOf(
                        mapOf("ja" to "安全カバーと非常停止ボタン", "en" to "Safety cover and emergency stop", "bn" to "নিরাপত্তা কভার এবং জরুরি বোতাম"),
                        mapOf("ja" to "スマートフォンの着信音", "en" to "Smartphone ringtone", "bn" to "স্মার্টফোনের রিংটোন"),
                        mapOf("ja" to "明日の天気予報", "en" to "Tomorrow's weather forecast", "bn" to "আগামীকালের আবহাওয়া পূর্বাভাস")
                    )
                ),
                CheckQuestion(
                    id = "q_${machine.id}_2",
                    termId = null,
                    prompt = mapOf(
                        "ja" to "異常な音がした場合はどうしますか？",
                        "en" to "What should you do if an abnormal sound occurs?",
                        "bn" to "অস্বাভাবিক শব্দ হলে আপনার কী করা উচিত?"
                    ),
                    options = listOf(
                        mapOf("ja" to "直ちに非常停止を押し職長に報告する", "en" to "Immediately press emergency stop and report to foreman", "bn" to "অবিলম্বে জরুরি বোতাম চাপুন এবং ফোরম্যানকে জানান"),
                        mapOf("ja" to "そのまま作業を続ける", "en" to "Continue working anyway", "bn" to "কাজ চালিয়ে যান"),
                        mapOf("ja" to "同僚に後で話す", "en" to "Tell a coworker later", "bn" to "পরে সহকর্মীকে বলুন")
                    )
                )
            )
        )
        return AppResult.Success(detail)
    }

    override suspend fun scan(machineId: String): AppResult<Unit> {
        if (delayMs > 0) delay(delayMs)
        val exists = machines.any { it.id == machineId }
        return if (exists) AppResult.Success(Unit) else AppResult.Failure(AppError.NotFound)
    }
}
