package com.techtsunagu.wakatta.feature.notifications

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
internal data class ContactPageWire(
    @SerialName("items") val items: List<ContactItemWire> = emptyList(),
)

@Serializable
internal data class ContactItemWire(
    @SerialName("id") val id: String,
    @SerialName("displayName") val displayName: String,
)

internal interface NotificationContactsApi {
    @GET(ApiPaths.CONTACTS)
    suspend fun listContacts(): Response<ApiEnvelope<ContactPageWire>>
}

@Singleton
class RealSenderNames @Inject constructor(
    private val retrofit: dagger.Lazy<Retrofit>,
    private val workerRepo: dagger.Lazy<WorkerRepository>,
    private val languages: LanguageManager,
) : SenderNames {

    private val cache = ConcurrentHashMap<String, String>()

    override suspend fun nameOf(senderId: String): String? {
        if (senderId.isBlank()) return NotificationTextBuilder.genericTitle(languages.currentLanguage.value)
        cache[senderId]?.let { return it }

        runCatching {
            val api = retrofit.get().create(NotificationContactsApi::class.java)
            val resp = api.listContacts()
            if (resp.isSuccessful) {
                resp.body()?.data?.items?.forEach { contact ->
                    cache[contact.id] = contact.displayName
                }
            }
        }
        cache[senderId]?.let { return it }

        runCatching {
            val res = workerRepo.get().getWorkers()
            if (res is AppResult.Success) {
                res.value.forEach { worker ->
                    cache[worker.id] = worker.displayName
                }
            }
        }
        cache[senderId]?.let { return it }

        return NotificationTextBuilder.genericTitle(languages.currentLanguage.value)
    }
}
