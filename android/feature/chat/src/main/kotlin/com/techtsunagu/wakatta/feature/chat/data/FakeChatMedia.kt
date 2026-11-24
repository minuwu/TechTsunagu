package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.Speaker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSpeaker @Inject constructor() : Speaker {
    private var speaking = false

    override fun speak(text: String, languageTag: String) {
        speaking = true
    }

    override fun isAvailable(languageTag: String): Boolean = true

    override fun stop() {
        speaking = false
    }
}
