package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

@Serializable
data object MeKey : NavKey

@Serializable
data object OnboardingKey : NavKey

/** Foreman: the shop's Workers and Foremen, to add, rename, reset a PIN or remove. */
@Serializable
data object MembersKey : NavKey
