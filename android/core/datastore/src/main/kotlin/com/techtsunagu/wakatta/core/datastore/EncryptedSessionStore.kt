package com.techtsunagu.wakatta.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import java.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class EncryptedSessionStore(
    private val dataStore: DataStore<Preferences>,
    private val aead: Aead,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : SessionStore {

    private val mutex = Mutex()
    private val _session = MutableStateFlow<Session?>(null)
    override val session: StateFlow<Session?> = _session.asStateFlow()

    @Volatile
    private var cachedToken: String? = null

    init {
        scope.launch {
            try {
                val initialPrefs = dataStore.data.first()
                updateFromPreferences(initialPrefs)
            } catch (_: Exception) {
                _session.value = null
                cachedToken = null
            }
        }
    }

    private fun updateFromPreferences(prefs: Preferences) {
        val encryptedBase64 = prefs[KEY_ENCRYPTED_SESSION]
        if (encryptedBase64.isNullOrEmpty()) {
            _session.value = null
            cachedToken = null
            return
        }
        try {
            val ciphertext = Base64.getDecoder().decode(encryptedBase64)
            val decryptedBytes = aead.decrypt(ciphertext, ASSOCIATED_DATA)
            val sessionObj = json.decodeFromString<Session>(decryptedBytes.decodeToString())
            _session.value = sessionObj
            cachedToken = sessionObj.token
        } catch (_: Exception) {
            _session.value = null
            cachedToken = null
        }
    }

    override suspend fun save(session: Session) {
        mutex.withLock {
            val plaintext = json.encodeToString(session).toByteArray(Charsets.UTF_8)
            val ciphertext = aead.encrypt(plaintext, ASSOCIATED_DATA)
            val base64 = Base64.getEncoder().encodeToString(ciphertext)
            dataStore.edit { prefs ->
                prefs[KEY_ENCRYPTED_SESSION] = base64
            }
            _session.value = session
            cachedToken = session.token
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            dataStore.edit { prefs ->
                prefs.remove(KEY_ENCRYPTED_SESSION)
            }
            _session.value = null
            cachedToken = null
        }
    }

    override fun getCachedToken(): String? = cachedToken

    companion object {
        private val KEY_ENCRYPTED_SESSION = stringPreferencesKey("wakatta_encrypted_session")
        private val ASSOCIATED_DATA = "wakatta_session_aead".toByteArray(Charsets.UTF_8)
    }
}
