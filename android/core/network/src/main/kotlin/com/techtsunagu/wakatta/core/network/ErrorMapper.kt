package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.Response

/** Routes where a 401 means "wrong PIN", not "session over" (contract 2.9). */
internal val wrongPinRoutes = setOf(ApiPaths.LOGIN, ApiPaths.ME_PIN)

/** Maps one failed response to an [AppError]. Status first, `error.code` where one status has several meanings. */
internal fun mapError(
    status: Int,
    code: String?,
    details: JsonObject?,
    retryAfter: String?,
    path: String,
    message: String? = null,
): AppError = when {
    status == 401 -> if (path in wrongPinRoutes) AppError.WrongPin else AppError.SessionExpired
    status == 403 -> if (code == "PIN_CHANGE_REQUIRED") AppError.PinChangeRequired else AppError.Forbidden
    status == 404 -> AppError.NotFound
    status == 409 -> AppError.Conflict(code ?: "CONFLICT")
    status == 412 -> AppError.PreconditionFailed
    status == 413 -> AppError.PayloadTooLarge
    status == 415 -> AppError.UnsupportedMedia
    status == 422 -> AppError.Validation(validationFields(details))
    status == 423 -> AppError.PinLocked((details?.get("until") as? JsonPrimitive)?.contentOrNull.orEmpty())
    status == 426 -> AppError.UpgradeRequired
    status == 429 -> AppError.RateLimited(retryAfter?.trim()?.toLongOrNull())
    status == 503 && code == "BUSY" -> AppError.Busy
    status >= 500 -> AppError.Server
    else -> AppError.Unknown(message ?: code)
}

/** `details.fields[].loc` last element of each entry, for example `pin`. */
private fun validationFields(details: JsonObject?): List<String> =
    (details?.get("fields") as? JsonArray).orEmpty().mapNotNull { entry ->
        val loc = (entry as? JsonObject)?.get("loc") as? JsonArray
        (loc?.lastOrNull() as? JsonPrimitive)?.contentOrNull
    }

internal fun Response<*>.toAppError(): AppError {
    val text = runCatching { errorBody()?.string() }.getOrNull()
    val body = text?.let { runCatching { WakattaJson.decodeFromString<ErrorEnvelope>(it).error }.getOrNull() }
    return mapError(
        status = code(),
        code = body?.code,
        details = body?.details,
        retryAfter = headers()["Retry-After"],
        path = raw().request.url.encodedPath,
        message = body?.message,
    )
}
