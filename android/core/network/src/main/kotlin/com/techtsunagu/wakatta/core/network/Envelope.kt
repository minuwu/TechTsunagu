package com.techtsunagu.wakatta.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Success body of every JSON route: `{success, data, message}`. `data` is null for routes that return nothing. */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = true,
    val data: T? = null,
    val message: String? = null,
)

/** Failure body: `{success:false, error:{code, message, details}}`. */
@Serializable
internal data class ErrorEnvelope(val error: ErrorBody? = null)

@Serializable
internal data class ErrorBody(
    val code: String? = null,
    val message: String? = null,
    val details: JsonObject? = null,
)
