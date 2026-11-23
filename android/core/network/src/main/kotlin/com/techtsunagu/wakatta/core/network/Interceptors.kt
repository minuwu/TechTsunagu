package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

/** Tag a Retrofit call with `@Tag key: IdempotencyKey`; pass the same value on every retry of one user action. */
class IdempotencyKey(val value: String)

private val publicPaths = setOf(ApiPaths.LOGIN, ApiPaths.ROSTER)

internal class AppHeadersInterceptor(
    private val versionName: String,
    private val language: () -> String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder()
            .header("X-App-Version", versionName)
            .header("X-App-Platform", "android")
            .header("Accept-Language", language())
            .build(),
    )
}

internal class BearerInterceptor(private val token: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val value = token()
        if (value == null || request.url.encodedPath in publicPaths) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer $value").build())
    }
}

internal class IdempotencyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val key = request.tag(IdempotencyKey::class.java) ?: return chain.proceed(request)
        return chain.proceed(request.newBuilder().header("Idempotency-Key", key.value).build())
    }
}

/**
 * A 401 on a call that carried the current token ends the session. A wrong PIN (login, PIN change)
 * must not, and a stale token must not clear a newer session.
 */
internal class SessionExpiryInterceptor(private val store: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val sent = request.header("Authorization")
        val current = store.getCachedToken()
        val ownsSession = sent != null && sent == "Bearer $current"
        if (response.code == 401 && request.url.encodedPath !in wrongPinRoutes && ownsSession) {
            runBlocking { store.clear() }
        }
        return response
    }
}

/** Debug only. Bodies under the auth prefix are never logged; `Authorization` is always redacted. */
internal fun loggingInterceptor(): Interceptor {
    fun logger(level: HttpLoggingInterceptor.Level) = HttpLoggingInterceptor().apply {
        this.level = level
        redactHeader("Authorization")
    }
    val full = logger(HttpLoggingInterceptor.Level.HEADERS)
    val basic = logger(HttpLoggingInterceptor.Level.BASIC)
    return Interceptor { chain ->
        val inAuth = chain.request().url.encodedPath.startsWith(ApiPaths.AUTH_PREFIX)
        (if (inAuth) basic else full).intercept(chain)
    }
}
