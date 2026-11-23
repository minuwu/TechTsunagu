package com.techtsunagu.wakatta.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.techtsunagu.wakatta.core.model.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DataStoreSettingsStore(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : SettingsStore {

    override val isDarkMode: StateFlow<Boolean?> = dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE]
    }.stateIn(scope, SharingStarted.Eagerly, null)

    override val isLargeText: StateFlow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_LARGE_TEXT] ?: false
    }.stateIn(scope, SharingStarted.Eagerly, false)

    override val isOfflineHintEnabled: StateFlow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_OFFLINE_HINT] ?: true
    }.stateIn(scope, SharingStarted.Eagerly, true)

    override val isNotificationEnabled: StateFlow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATION] ?: true
    }.stateIn(scope, SharingStarted.Eagerly, true)

    override val isOnboardingDone: StateFlow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }.stateIn(scope, SharingStarted.Eagerly, false)

    override suspend fun setDarkMode(enabled: Boolean?) {
        dataStore.edit { prefs ->
            if (enabled == null) {
                prefs.remove(KEY_DARK_MODE)
            } else {
                prefs[KEY_DARK_MODE] = enabled
            }
        }
    }

    override suspend fun setLargeText(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_LARGE_TEXT] = enabled }
    }

    override suspend fun setOfflineHint(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_OFFLINE_HINT] = enabled }
    }

    override suspend fun setNotification(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_NOTIFICATION] = enabled }
    }

    override suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_ONBOARDING_DONE] = done }
    }

    companion object {
        private val KEY_DARK_MODE = booleanPreferencesKey("pref_dark_mode")
        private val KEY_LARGE_TEXT = booleanPreferencesKey("pref_large_text")
        private val KEY_OFFLINE_HINT = booleanPreferencesKey("pref_offline_hint")
        private val KEY_NOTIFICATION = booleanPreferencesKey("pref_notification")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("pref_onboarding_done")
    }
}
