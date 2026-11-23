package com.techtsunagu.wakatta.feature.update

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

/**
 * What a remote source says about the newest app. [minSupportedVersion] is a version name for
 * `/app/config` and a version code (as text) for the static manifest.
 * [downloadUrl] and [sha256] are null when the source does not offer an APK.
 */
data class UpdateOffer(
    val latestVersionName: String,
    val minSupportedVersion: String,
    val downloadUrl: String?,
    val sha256: String?,
    val level: VersionGateResult,
)

/**
 * Wire DTO from `GET /api/app/config` matching docs/openapi.yaml schema Envelope[AppConfig].
 */
@Serializable
data class AppConfigDto(
    val minVersion: String,
    val latestVersion: String,
    val downloadUrl: String? = null,
    val sha256: String? = null,
    val serverTime: String? = null,
)

/**
 * Retrofit definition for the public version gate route.
 */
interface AppConfigApi {
    @GET(ApiPaths.APP_CONFIG)
    suspend fun getAppConfig(): Response<ApiEnvelope<AppConfigDto>>
}

interface AppConfigRepository {
    suspend fun fetch(): AppResult<UpdateOffer>
}

/** Where the live `/app/config` URL comes from (kept for test / fallback compatibility). */
interface UpdateEndpoints {
    /** Full URL, or blank when not configured (the static manifest is then the only source). */
    fun appConfigUrl(): String
}
