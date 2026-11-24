package com.techtsunagu.wakatta.core.common

data class AppConfig(
    val useMock: Boolean,
    val apiBaseUrl: String,
    val updateManifestUrl: String,
    val versionName: String,
    val versionCode: Int,
    val isDebug: Boolean
)
