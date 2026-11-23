package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSettingsStore @Inject constructor() : SettingsStore {

    private val _isDarkMode = MutableStateFlow<Boolean?>(false)
    override val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _isLargeText = MutableStateFlow(false)
    override val isLargeText: StateFlow<Boolean> = _isLargeText.asStateFlow()

    private val _isOfflineHintEnabled = MutableStateFlow(false)
    override val isOfflineHintEnabled: StateFlow<Boolean> = _isOfflineHintEnabled.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    override val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _isOnboardingDone = MutableStateFlow(false)
    override val isOnboardingDone: StateFlow<Boolean> = _isOnboardingDone.asStateFlow()

    private val _isNotifyMessages = MutableStateFlow(true)
    val isNotifyMessages: StateFlow<Boolean> = _isNotifyMessages.asStateFlow()

    private val _isNotifySafety = MutableStateFlow(true)
    val isNotifySafety: StateFlow<Boolean> = _isNotifySafety.asStateFlow()

    private val _isNotifyLife = MutableStateFlow(true)
    val isNotifyLife: StateFlow<Boolean> = _isNotifyLife.asStateFlow()

    override suspend fun setDarkMode(enabled: Boolean?) {
        _isDarkMode.value = enabled
    }

    override suspend fun setLargeText(enabled: Boolean) {
        _isLargeText.value = enabled
    }

    override suspend fun setOfflineHint(enabled: Boolean) {
        _isOfflineHintEnabled.value = enabled
    }

    override suspend fun setNotification(enabled: Boolean) {
        _isNotificationEnabled.value = enabled
    }

    override suspend fun setOnboardingDone(done: Boolean) {
        _isOnboardingDone.value = done
    }

    fun setNotifyMessages(enabled: Boolean) {
        _isNotifyMessages.value = enabled
    }

    fun setNotifySafety(enabled: Boolean) {
        _isNotifySafety.value = enabled
    }

    fun setNotifyLife(enabled: Boolean) {
        _isNotifyLife.value = enabled
    }
}
