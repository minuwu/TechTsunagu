package com.techtsunagu.wakatta.feature.board

import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestSessionStore(
    initialSession: Session? = Session(
        token = "test-token",
        user = User(
            id = "u_tanaka",
            displayName = "田中 健一",
            role = Role.FOREMAN,
            language = "ja",
            shopId = "1",
            districtId = "1"
        )
    )
) : SessionStore {
    private val _session = MutableStateFlow(initialSession)
    override val session: StateFlow<Session?> = _session.asStateFlow()

    override suspend fun save(session: Session) {
        _session.value = session
    }

    override suspend fun clear() {
        _session.value = null
    }

    override fun getCachedToken(): String? = _session.value?.token

    fun setWorkerSession() {
        _session.value = Session(
            token = "worker-token",
            user = User(
                id = "u_rahim",
                displayName = "Rahim Uddin",
                role = Role.WORKER,
                language = "bn",
                shopId = "1",
                districtId = "1"
            )
        )
    }

    fun setForemanSession() {
        _session.value = Session(
            token = "foreman-token",
            user = User(
                id = "u_tanaka",
                displayName = "田中 健一",
                role = Role.FOREMAN,
                language = "ja",
                shopId = "1",
                districtId = "1"
            )
        )
    }
}

class TestLanguageManager(initialLang: String = "ja") : LanguageManager {
    private val _currentLanguage = MutableStateFlow(initialLang)
    override val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    override suspend fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }
}
