package com.techtsunagu.wakatta.feature.update

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.VersionGateState
import com.techtsunagu.wakatta.core.model.VersionInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.techtsunagu.wakatta.core.model.VersionGate as ModelVersionGate

/**
 * The app-wide gate. `:app` calls [refresh] after the first frame; `:core:network` calls [require]
 * when a response is 426. A failed refresh never blocks the app: the last state is kept.
 */
@Singleton
class UpdateVersionGate @Inject constructor(
    private val repository: AppConfigRepository,
) : ModelVersionGate {
    private val _state = MutableStateFlow<VersionGateState>(VersionGateState.UpToDate)
    override val state: StateFlow<VersionGateState> = _state.asStateFlow()

    override fun require() {
        _state.value = VersionGateState.Hard(lastInfo() ?: VersionInfo("", "", ""))
    }

    suspend fun refresh(): AppResult<UpdateOffer> {
        val result = repository.fetch()
        when (result) {
            is AppResult.Success -> _state.value = result.value.toState()
            is AppResult.Failure -> if (result.error == AppError.UpgradeRequired) require()
        }
        return result
    }

    private fun lastInfo(): VersionInfo? = when (val s = _state.value) {
        is VersionGateState.Soft -> s.info
        is VersionGateState.Hard -> s.info
        VersionGateState.UpToDate -> null
    }

    private fun UpdateOffer.toState(): VersionGateState {
        val info = VersionInfo(latestVersionName, downloadUrl.orEmpty(), minSupportedVersion)
        return when (level) {
            VersionGateResult.UpToDate -> VersionGateState.UpToDate
            VersionGateResult.Soft -> VersionGateState.Soft(info)
            VersionGateResult.Hard -> VersionGateState.Hard(info)
        }
    }
}
