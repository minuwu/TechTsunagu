package com.techtsunagu.wakatta.feature.notifications

import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Query

@Serializable
data class DeviceInDto(
    @SerialName("installId") val installId: String,
    @SerialName("platform") val platform: String,
    @SerialName("fcmToken") val fcmToken: String,
    @SerialName("appVersion") val appVersion: String,
    @SerialName("osApiLevel") val osApiLevel: Int? = null,
    @SerialName("locale") val locale: String? = null,
    @SerialName("notificationsEnabled") val notificationsEnabled: Boolean,
)

@Serializable
data class DeviceOutDto(
    @SerialName("registered") val registered: Boolean,
    @SerialName("lastSeenAt") val lastSeenAt: String,
)

interface DeviceApi {
    @PUT(ApiPaths.DEVICES_ME)
    suspend fun registerDevice(@Body body: DeviceInDto): Response<ApiEnvelope<DeviceOutDto>>

    @DELETE(ApiPaths.DEVICES_ME)
    suspend fun unregisterDevice(@Query("installId") installId: String): Response<ApiEnvelope<Unit>>
}
