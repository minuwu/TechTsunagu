package com.techtsunagu.wakatta.core.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.RequestBody
import java.lang.reflect.Type

/** Responses and ordinary requests: lenient reading, no `null` or default written to the wire. */
val WakattaJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    encodeDefaults = false
}

/**
 * For the few request bodies where `null` means "clear" (contract 2.4). Mark the Retrofit parameter
 * with [ExplicitNulls]. Defaults are written too, so use it only on DTOs that carry no UI-only default.
 */
val WakattaJsonExplicitNulls: Json = Json(WakattaJson) {
    explicitNulls = true
    encodeDefaults = true
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExplicitNulls

internal class ExplicitNullsConverterFactory : Converter.Factory() {
    private val delegate = WakattaJsonExplicitNulls.asConverterFactory("application/json".toMediaType())

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? =
        if (parameterAnnotations.any { it is ExplicitNulls }) {
            delegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        } else {
            null
        }
}
