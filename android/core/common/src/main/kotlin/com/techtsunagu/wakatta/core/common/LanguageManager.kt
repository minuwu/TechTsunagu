package com.techtsunagu.wakatta.core.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LanguageManagerImpl(initialLang: String = "ja") : com.techtsunagu.wakatta.core.model.LanguageManager {
    private val _currentLanguage = MutableStateFlow(initialLang)
    override val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    override suspend fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }
}
