package com.techtsunagu.wakatta.core.deeplink

import kotlinx.serialization.Serializable

/**
 * A parsed Wakatta? link (MOBILE_PLATFORM_CONTRACT 2.6). This object, not the raw string, is what the
 * app stores as `pendingLink`, so it is serializable. Ids are numeric strings, the contact token is
 * 22 characters of base64url.
 */
@Serializable
sealed interface AppLink {
    @Serializable
    data class Machine(val id: String, val shopCode: String?) : AppLink

    @Serializable
    data class Contact(val token: String) : AppLink

    @Serializable
    data class Thread(val id: String, val messageId: String?) : AppLink

    @Serializable
    data class Notice(val id: String, val eventId: String?) : AppLink

    @Serializable
    data class Briefing(val id: String) : AppLink

    @Serializable
    data class NearMiss(val id: String) : AppLink

    @Serializable
    data object InterviewsDue : AppLink
}

/** Why a string was refused. The app shows nothing for these; the scanner maps them to "not a Wakatta? code". */
enum class DeepLinkRejection {
    TOO_LONG,
    ILLEGAL_CHARACTER,
    NOT_HTTPS,
    WRONG_HOST,
    UNKNOWN_PATH,
    MALFORMED_ID,
    MALFORMED_PARAM,
    DUPLICATE_PARAM,
    MISSING_LINK,
    UNSUPPORTED_VERSION,
}

sealed interface DeepLinkResult {
    data class Matched(val link: AppLink) : DeepLinkResult
    data class Rejected(val reason: DeepLinkRejection) : DeepLinkResult
}
