package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.SessionStore
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Builds the HTTP stack. Hilt uses it ([NetworkModule]); tests call it directly against a MockWebServer. */
object ApiClientFactory {

    /** Longer than the server takes for an AI draft (25 s, then a degraded answer: G1), so a slow draft still
     *  arrives instead of a timeout for a message the server goes on to store (ANDROID_FIX_PLAN R1). */
    const val READ_TIMEOUT_SECONDS = 60L

    /** Interceptor order is fixed: app headers, bearer, idempotency, logging (debug only), session expiry. */
    fun okHttp(
        versionName: String,
        language: () -> String,
        sessionStore: SessionStore,
        debug: Boolean,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(AppHeadersInterceptor(versionName, language))
        .addInterceptor(BearerInterceptor { sessionStore.getCachedToken() })
        .addInterceptor(IdempotencyInterceptor())
        .apply { if (debug) addInterceptor(loggingInterceptor()) }
        .addInterceptor(SessionExpiryInterceptor(sessionStore))
        .build()

    /** [baseUrl] has no trailing slash; paths from `ApiConfig.ApiPaths` start with one. */
    fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.trimEnd('/') + "/")
        .client(client)
        .addConverterFactory(ExplicitNullsConverterFactory())
        .addConverterFactory(WakattaJson.asConverterFactory("application/json".toMediaType()))
        .build()
}
