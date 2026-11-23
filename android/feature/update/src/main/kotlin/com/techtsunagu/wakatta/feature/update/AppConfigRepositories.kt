package com.techtsunagu.wakatta.feature.update

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.network.safeApiCall
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request

/** GET [url] and parse the body as a JSON object; HTTP and IO failures become [AppError]. */
internal suspend fun Call.Factory.getJsonObject(
    url: String,
    headers: Map<String, String> = emptyMap(),
): AppResult<JsonObject> {
    val httpUrl = url.toHttpUrlOrNull() ?: return AppResult.Failure(AppError.NotFound)
    val request = Request.Builder().url(httpUrl).apply { headers.forEach { (k, v) -> header(k, v) } }.build()
    return withContext(Dispatchers.IO) {
        try {
            newCall(request).execute().use { response ->
                when {
                    response.code == 426 -> AppResult.Failure(AppError.UpgradeRequired)
                    response.code >= 500 -> AppResult.Failure(AppError.Server)
                    !response.isSuccessful -> AppResult.Failure(AppError.Unknown("HTTP ${response.code}"))
                    else -> AppResult.Success(Json.parseToJsonElement(response.body.string()).jsonObject)
                }
            }
        } catch (_: SocketTimeoutException) {
            AppResult.Failure(AppError.Timeout)
        } catch (_: IOException) {
            AppResult.Failure(AppError.Offline)
        } catch (_: IllegalArgumentException) {
            // kotlinx.serialization errors and a non-object body both end here.
            AppResult.Failure(AppError.Unknown("bad json"))
        }
    }
}

private fun JsonObject.text(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

/** Static `latest.json` (`versionName`, `versionCode`, `apkFile`, `sha256`, `minSupportedVersionCode`). */
internal class StaticManifestAppConfigRepository(
    private val client: Call.Factory,
    private val manifestUrl: String,
    private val currentVersionCode: Long,
) : AppConfigRepository {
    override suspend fun fetch(): AppResult<UpdateOffer> {
        if (manifestUrl.isBlank()) return AppResult.Failure(AppError.NotFound)
        return when (val r = client.getJsonObject(manifestUrl)) {
            is AppResult.Failure -> r
            is AppResult.Success -> parse(r.value)
        }
    }

    private fun parse(json: JsonObject): AppResult<UpdateOffer> {
        val latestCode = json.text("versionCode")?.toLongOrNull()
        val minCode = json["minSupportedVersionCode"]?.jsonPrimitive?.longOrNull
        val name = json.text("versionName")
        if (latestCode == null || minCode == null || name == null) {
            return AppResult.Failure(AppError.Unknown("bad manifest"))
        }
        val apkUrl = json.text("apkFile")?.let { manifestUrl.toHttpUrlOrNull()?.resolve(it)?.toString() }
        return AppResult.Success(
            UpdateOffer(
                latestVersionName = name,
                minSupportedVersion = minCode.toString(),
                downloadUrl = apkUrl,
                sha256 = json.text("sha256"),
                level = VersionGate.evaluate(currentVersionCode, minCode, latestCode),
            ),
        )
    }
}

/**
 * `GET /api/app/config` through Retrofit of `:core:network`.
 * Wire schema: `{data: {minVersion, latestVersion, downloadUrl, sha256, serverTime}}`.
 */
internal class ApiAppConfigRepository(
    private val api: () -> AppConfigApi,
    private val currentVersionName: String,
) : AppConfigRepository {
    constructor(api: AppConfigApi, currentVersionName: String) : this({ api }, currentVersionName)

    override suspend fun fetch(): AppResult<UpdateOffer> {
        return when (val result = safeApiCall { api().getAppConfig() }) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val data = result.value
                AppResult.Success(
                    UpdateOffer(
                        latestVersionName = data.latestVersion,
                        minSupportedVersion = data.minVersion,
                        downloadUrl = data.downloadUrl?.ifBlank { null },
                        sha256 = data.sha256?.ifBlank { null },
                        level = VersionGate.evaluate(currentVersionName, data.minVersion, data.latestVersion),
                    ),
                )
            }
        }
    }
}

/** The backend first; the static manifest only when the backend is unreachable or unconfigured. */
internal class FallbackAppConfigRepository(
    private val primary: AppConfigRepository,
    private val fallback: AppConfigRepository,
) : AppConfigRepository {
    override suspend fun fetch(): AppResult<UpdateOffer> {
        val first = primary.fetch()
        // 426 is a real answer from the server; every other failure may be a dead backend.
        if (first is AppResult.Success || first.errorOrNull() == AppError.UpgradeRequired) return first
        return when (val second = fallback.fetch()) {
            is AppResult.Success -> second
            is AppResult.Failure -> first
        }
    }
}

/** Debug only: a newer version and a simulated download, so the screens work without a server. */
internal class FakeAppConfigRepository(
    private val level: VersionGateResult = VersionGateResult.Soft,
    private val latestVersion: String = "9.9.9",
    private val minSupportedVersion: String = "1.0.0",
) : AppConfigRepository {
    override suspend fun fetch(): AppResult<UpdateOffer> = AppResult.Success(
        UpdateOffer(
            latestVersionName = latestVersion,
            minSupportedVersion = minSupportedVersion,
            downloadUrl = "fake://wakatta.apk",
            sha256 = "fake",
            level = level,
        ),
    )
}
