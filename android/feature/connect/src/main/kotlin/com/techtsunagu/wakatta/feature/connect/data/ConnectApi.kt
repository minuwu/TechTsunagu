package com.techtsunagu.wakatta.feature.connect.data

import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/** Connect and Contacts routes of docs/openapi.yaml. */
internal interface ConnectApi {

    /** My QR: make it or rotate it. */
    @POST(ApiPaths.ME_QR)
    suspend fun myQr(
        @Body body: QrInWire,
    ): Response<ApiEnvelope<QrOutWire>>

    /** Who a scanned QR belongs to (name and language only). */
    @GET(ApiPaths.CONTACTS_RESOLVE)
    suspend fun resolve(
        @Query("token") token: String,
    ): Response<ApiEnvelope<ContactPreviewWire>>

    /** Add the Contact a scanned QR belongs to. */
    @POST(ApiPaths.CONTACTS)
    suspend fun addContact(
        @Tag key: IdempotencyKey,
        @Body body: ContactCreateWire,
    ): Response<ApiEnvelope<ContactOutWire>>

    /** List Contacts with unread counts. */
    @GET(ApiPaths.CONTACTS)
    suspend fun listContacts(
        @Query("limit") limit: Int = 50,
        @Query("cursor") cursor: String? = null,
    ): Response<ApiEnvelope<PageWire<ContactOutWire>>>

    /** Mute or hide a Contact. */
    @PUT(ApiPaths.CONTACT)
    suspend fun updateContact(
        @Path("contact_id") contactId: String,
        @Body body: ContactUpdateWire,
    ): Response<ApiEnvelope<ContactOutWire>>

    /** Block a Contact. */
    @POST(ApiPaths.BLOCKS)
    suspend fun block(
        @Body body: BlockInWire,
    ): Response<ApiEnvelope<Unit>>

    /** Unblock a Contact. */
    @DELETE(ApiPaths.BLOCK)
    suspend fun unblock(
        @Path("contact_id") contactId: String,
    ): Response<ApiEnvelope<Unit>>

    /** Raw body: `ConditionalGet` reads ETag and 304. */
    @GET(ApiPaths.CONTACT_MESSAGES)
    suspend fun readMessages(
        @Path("contact_id") contactId: String,
        @Header("If-None-Match") ifNoneMatch: String?,
        @Query("after") after: String? = null,
    ): Response<ResponseBody>

    /** Delete one message (ADR 0014). */
    @DELETE(ApiPaths.CONTACT_MESSAGE)
    suspend fun deleteMessage(
        @Path("contact_id") contactId: String,
        @Path("message_id") messageId: String,
    ): Response<ApiEnvelope<Unit>>

    /** Delete the whole Contact Thread on this side (ADR 0014). */
    @DELETE(ApiPaths.CONTACT_MESSAGES)
    suspend fun clearMessages(@Path("contact_id") contactId: String): Response<ApiEnvelope<Unit>>

    /** Send a message in a Contact Thread. */
    @POST(ApiPaths.CONTACT_MESSAGES)
    suspend fun sendMessage(
        @Path("contact_id") contactId: String,
        @Tag key: IdempotencyKey,
        @Body body: ContactMessageInWire,
    ): Response<ApiEnvelope<ContactMessageOutWire>>

    /** List Talk Threads, newest first. */
    @GET(ApiPaths.TALK_THREADS)
    suspend fun listTalks(
        @Query("limit") limit: Int = 50,
        @Query("cursor") cursor: String? = null,
    ): Response<ApiEnvelope<PageWire<TalkThreadOutWire>>>

    /** Create a Talk Thread. */
    @POST(ApiPaths.TALK_THREADS)
    suspend fun createTalk(
        @Tag key: IdempotencyKey,
        @Body body: TalkThreadInWire,
    ): Response<ApiEnvelope<TalkThreadOutWire>>

    /** Rename a Talk Thread. */
    @PATCH(ApiPaths.TALK_THREAD)
    suspend fun renameTalk(
        @Path("thread_id") threadId: String,
        @Body body: TalkThreadInWire,
    ): Response<ApiEnvelope<TalkThreadOutWire>>

    /** Delete a Talk Thread. */
    @DELETE(ApiPaths.TALK_THREAD)
    suspend fun deleteTalk(
        @Path("thread_id") threadId: String,
    ): Response<ApiEnvelope<Unit>>

    /** List entries in a Talk Thread, oldest first. */
    @GET(ApiPaths.TALK_ENTRIES)
    suspend fun listEntries(
        @Path("thread_id") threadId: String,
        @Query("limit") limit: Int = 50,
        @Query("cursor") cursor: String? = null,
    ): Response<ApiEnvelope<PageWire<TalkEntryOutWire>>>

    /** Add an entry to a Talk Thread (multipart, sends text). */
    @Multipart
    @POST(ApiPaths.TALK_ENTRIES)
    suspend fun addEntry(
        @Path("thread_id") threadId: String,
        @Tag key: IdempotencyKey,
        @Part text: MultipartBody.Part,
    ): Response<ApiEnvelope<TalkEntryOutWire>>

    /** Update an entry with exactly one of: ownText, replyJa, confirmed. */
    @PATCH(ApiPaths.TALK_ENTRY)
    suspend fun updateEntry(
        @Path("thread_id") threadId: String,
        @Path("entry_id") entryId: String,
        @Body body: TalkEntryUpdateWire,
    ): Response<ApiEnvelope<TalkEntryOutWire>>

    /** Delete one entry. */
    @DELETE(ApiPaths.TALK_ENTRY)
    suspend fun deleteEntry(
        @Path("thread_id") threadId: String,
        @Path("entry_id") entryId: String,
    ): Response<ApiEnvelope<Unit>>
}

@Serializable
internal data class PageWire<T>(
    val items: List<T> = emptyList(),
    val nextCursor: String? = null,
)

@Serializable
internal data class QrInWire(
    val rotate: Boolean = false,
)

@Serializable
internal data class QrOutWire(
    val token: String? = null,
    val createdAt: String = "",
)

@Serializable
internal data class ContactPreviewWire(
    val displayName: String,
    val language: String = "ja",
)

@Serializable
internal data class ContactCreateWire(
    val token: String,
)

@Serializable
internal data class ContactUpdateWire(
    val muted: Boolean? = null,
    val hidden: Boolean? = null,
)

@Serializable
internal data class ContactOutWire(
    val id: String,
    val displayName: String,
    val language: String = "ja",
    val muted: Boolean = false,
    val hidden: Boolean = false,
    val lastMessageAt: String? = null,
    val unread: Int = 0,
    val createdAt: String = "",
)

@Serializable
internal data class BlockInWire(
    val contactId: String,
)

@Serializable
internal data class ContactMessagesPageWire(
    val items: List<ContactMessageOutWire> = emptyList(),
    val nextCursor: String? = null,
)

@Serializable
internal data class ContactMessageInWire(
    val text: String,
    val lang: String,
)

@Serializable
internal data class ContactMessageOutWire(
    val id: String,
    val fromMe: Boolean = false,
    val textSrc: String = "",
    val srcLang: String = "ja",
    val text: String = "",
    val createdAt: String = "",
)

@Serializable
internal data class TalkThreadInWire(
    val title: String,
)

@Serializable
internal data class TalkThreadOutWire(
    val id: String,
    val title: String,
    val updatedAt: String = "",
    val entryCount: Int = 0,
)

@Serializable
internal data class DialectTermOutWire(
    val dialect: String,
    val plain: String,
    val meaning: String = "",
)

@Serializable
internal data class ReplyOptionOutWire(
    val ja: String,
    val meaning: String = "",
)

@Serializable
internal data class TalkEntryOutWire(
    val id: String,
    val transcript: String = "",
    val plainJa: String? = null,
    val meaning: String? = null,
    val terms: List<DialectTermOutWire> = emptyList(),
    val needsStaff: Boolean = false,
    val needsEdit: Boolean = false,
    val ownText: String? = null,
    val grade: String? = null,
    val replyOptions: List<ReplyOptionOutWire> = emptyList(),
    val replyJa: String? = null,
    val confirmed: Boolean? = null,
    val createdAt: String = "",
)

@Serializable
internal data class TalkEntryUpdateWire(
    val ownText: String? = null,
    val replyJa: String? = null,
    val confirmed: Boolean? = null,
)
