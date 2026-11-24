package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CacheStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Polling helper for routes with an ETag. The backend sends `Cache-Control: no-store`, so OkHttp's
 * own cache cannot help: body and ETag are kept in [CacheStore] and a 304 returns the stored body.
 * The ETag goes back verbatim, quotes included.
 */
class ConditionalGet(private val cache: CacheStore) {

    /** [call] receives the stored ETag (or null) for `If-None-Match` and returns the raw response. */
    suspend fun <T> get(
        key: String,
        serializer: KSerializer<T>,
        call: suspend (ifNoneMatch: String?) -> Response<ResponseBody>,
    ): AppResult<T> = guarded {
        val cached = cache.get(key)
        val response = call(cached?.etag)
        when {
            response.code() == 304 && cached != null -> decode(cached.json, serializer)
            response.isSuccessful -> {
                val text = withContext(Dispatchers.IO) { response.body()?.string().orEmpty() }
                decode(text, serializer).also { result ->
                    if (result.isSuccess) cache.put(key, text, response.headers()["ETag"])
                }
            }
            else -> AppResult.Failure(response.toAppError())
        }
    }

    private fun <T> decode(text: String, serializer: KSerializer<T>): AppResult<T> = try {
        val data = WakattaJson.decodeFromString(ApiEnvelope.serializer(serializer), text).data
        if (data == null) AppResult.Failure(AppError.Unknown("empty body")) else AppResult.Success(data)
    } catch (_: SerializationException) {
        AppResult.Failure(AppError.Unknown("bad response"))
    }
}
