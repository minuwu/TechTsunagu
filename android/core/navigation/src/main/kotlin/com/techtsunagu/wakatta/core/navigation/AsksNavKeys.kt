package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AskKey : NavKey

@Serializable
data object SenpaiKey : NavKey

/** Ask Wakatta, the chatbot (XREQ-016); from a Machine screen with that Machine's id. */
@Serializable
data class ChatbotKey(val machineId: String? = null) : NavKey
