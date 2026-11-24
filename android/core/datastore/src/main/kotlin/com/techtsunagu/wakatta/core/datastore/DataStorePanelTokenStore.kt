package com.techtsunagu.wakatta.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.techtsunagu.wakatta.core.model.PanelTokenStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DataStorePanelTokenStore(
    private val dataStore: DataStore<Preferences>
) : PanelTokenStore {

    override suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_PANEL_TOKEN] = token
        }
    }

    override suspend fun readToken(): String? {
        return dataStore.data.map { prefs -> prefs[KEY_PANEL_TOKEN] }.firstOrNull()
    }

    override suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_PANEL_TOKEN)
        }
    }

    companion object {
        private val KEY_PANEL_TOKEN = stringPreferencesKey("pref_panel_token")
    }
}
