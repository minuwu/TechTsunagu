package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import java.io.IOException
import java.io.InterruptedIOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.Response

/** Runs one Retrofit call and returns [AppResult]; expected failures never throw. */
suspend fun <T> safeApiCall(block: suspend () -> Response<ApiEnvelope<T>>): AppResult<T> = guarded {
    val response = block()
    if (!response.isSuccessful) return@guarded AppResult.Failure(response.toAppError())
    val data = response.body()?.data
    if (data == null) AppResult.Failure(AppError.Unknown("empty body")) else AppResult.Success(data)
}

/** Same for routes whose success body has `data: null`. */
suspend fun safeApiCallUnit(block: suspend () -> Response<ApiEnvelope<Unit>>): AppResult<Unit> = guarded {
    val response = block()
    if (response.isSuccessful) AppResult.Success(Unit) else AppResult.Failure(response.toAppError())
}

internal suspend fun <T> guarded(block: suspend () -> AppResult<T>): AppResult<T> = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (_: InterruptedIOException) {
    AppResult.Failure(AppError.Timeout)
} catch (_: IOException) {
    AppResult.Failure(AppError.Offline)
} catch (_: SerializationException) {
    AppResult.Failure(AppError.Unknown("bad response"))
} catch (e: Exception) {
    // a mapping bug or an odd response is a failed call, never a crash
    AppResult.Failure(AppError.Unknown(e.message))
}
