package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ChatsKey : NavKey

@Serializable
data class ChatKey(val threadId: String) : NavKey
