package com.techtsunagu.wakatta.core.deeplink

/**
 * The one place that turns a string into an [AppLink] (MOBILE_PLATFORM_CONTRACT 2.2 to 2.6).
 * Serves App Link intents, notification `data.link` and the QR scanner.
 *
 * Strict by design: https only, host must equal one of [allowedHosts] (no userinfo, no port, no
 * trailing dot), ASCII only, no percent-encoding in the path or in a known value, ids are digits,
 * nothing is fetched or followed. Unknown query keys and the fragment are ignored. A known query key
 * with a bad value, or given twice, rejects the whole link. Pure Kotlin, no Android types.
 */
class UriDeepLinkMatcher(allowedHosts: Set<String>) {

    private val hosts: Set<String> = allowedHosts.map { it.lowercase() }.toSet()

    init {
        require(hosts.isNotEmpty()) { "allowedHosts must not be empty" }
        require(hosts.all { isPlainHost(it) }) { "allowedHosts must be bare host names, got $hosts" }
    }

    constructor(host: String) : this(setOf(host))

    fun match(raw: String): DeepLinkResult {
        if (raw.length > MAX_LENGTH) return reject(DeepLinkRejection.TOO_LONG)
        // Control characters, spaces, DEL and everything non-ASCII (IDN homographs, full-width forms,
        // bidi overrides, zero-width characters) are refused before any parsing.
        if (raw.any { it.code <= 0x20 || it.code >= 0x7F }) return reject(DeepLinkRejection.ILLEGAL_CHARACTER)
        if (!raw.startsWith(HTTPS_PREFIX, ignoreCase = true)) return reject(DeepLinkRejection.NOT_HTTPS)

        val rest = raw.substring(HTTPS_PREFIX.length)
        val authorityEnd = rest.indexOfFirst { it == '/' || it == '?' || it == '#' }.let { if (it < 0) rest.length else it }
        if (rest.substring(0, authorityEnd).lowercase() !in hosts) return reject(DeepLinkRejection.WRONG_HOST)

        // The fragment never matters. Anything after '#' is dropped, including a '?' inside it.
        val afterAuthority = rest.substring(authorityEnd).substringBefore('#')
        val path = afterAuthority.substringBefore('?')
        val query = if ('?' in afterAuthority) afterAuthority.substringAfter('?') else ""

        if (!path.startsWith("/")) return reject(DeepLinkRejection.UNKNOWN_PATH)
        val segments = path.substring(1).split('/')
        val first = segments[0]
        if (first == "i") {
            return if (segments == listOf("i", "due")) matched(AppLink.InterviewsDue) else reject(DeepLinkRejection.UNKNOWN_PATH)
        }
        if (first !in ID_PATHS) return reject(DeepLinkRejection.UNKNOWN_PATH)
        if (segments.size != 2) return reject(DeepLinkRejection.UNKNOWN_PATH)
        val id = segments[1]

        if (first == "c") {
            val ok = SHORT_QR.matches(id) || (id.length in TOKEN_LENGTHS && id.all { isBase64Url(it) })
            return if (ok) matched(AppLink.Contact(id)) else reject(DeepLinkRejection.MALFORMED_ID)
        }
        if (!isNumericId(id)) return reject(DeepLinkRejection.MALFORMED_ID)

        return when (first) {
            "m" -> optional(query, "s", ::isShopCode) { AppLink.Machine(id, it) }
            "t" -> optional(query, "m", ::isNumericId) { AppLink.Thread(id, it) }
            "n" -> optional(query, "e", ::isNumericId) { AppLink.Notice(id, it) }
            "b" -> matched(AppLink.Briefing(id))
            else -> matched(AppLink.NearMiss(id))
        }
    }

    /**
     * Notification payload (MOBILE_PLATFORM_CONTRACT 1.5): `data.v` must be "1" and `data.link` is the
     * only navigation input. Other keys are ignored.
     */
    fun matchNotification(data: Map<String, String>): DeepLinkResult {
        if (data["v"] != SUPPORTED_PUSH_VERSION) return reject(DeepLinkRejection.UNSUPPORTED_VERSION)
        val link = data["link"] ?: return reject(DeepLinkRejection.MISSING_LINK)
        return match(link)
    }

    /** Reads the one known query [key] (absent is fine) and builds the link from its validated value. */
    private inline fun optional(
        query: String,
        key: String,
        isValid: (String) -> Boolean,
        build: (String?) -> AppLink,
    ): DeepLinkResult {
        var value: String? = null
        for (pair in query.split('&')) {
            if (pair.substringBefore('=') != key) continue
            if (value != null) return reject(DeepLinkRejection.DUPLICATE_PARAM)
            val v = pair.substringAfter('=', "")
            if (!isValid(v)) return reject(DeepLinkRejection.MALFORMED_PARAM)
            value = v
        }
        return matched(build(value))
    }

    private fun matched(link: AppLink): DeepLinkResult = DeepLinkResult.Matched(link)

    private fun reject(reason: DeepLinkRejection): DeepLinkResult = DeepLinkResult.Rejected(reason)

    companion object {
        const val MAX_LENGTH = 2048
        private const val HTTPS_PREFIX = "https://"
        private const val SUPPORTED_PUSH_VERSION = "1"
        // token_urlsafe(16) and token_urlsafe(32): the server's My QR token is the 43-character one
        private val TOKEN_LENGTHS = setOf(22, 43)

        /** My QR code now: WK-7KQ4M2. */
        private val SHORT_QR = Regex("WK-[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}")
        private const val SHOP_CODE_LENGTH = 8
        private const val MAX_ID_DIGITS = 18
        private val ID_PATHS = setOf("m", "c", "t", "n", "b", "nm")

        private fun isNumericId(s: String) = s.length in 1..MAX_ID_DIGITS && s.all { it in '0'..'9' }

        private fun isShopCode(s: String) =
            s.length == SHOP_CODE_LENGTH && s.all { it in 'A'..'Z' || it in '0'..'9' }

        private fun isBase64Url(c: Char) = c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '_' || c == '-'

        private fun isPlainHost(h: String) =
            h.isNotEmpty() && !h.startsWith('.') && !h.endsWith('.') && !h.startsWith('-') &&
                h.all { it in 'a'..'z' || it in '0'..'9' || it == '.' || it == '-' }
    }
}

/** Contract signature (2.6). Null means "not ours": open Home or reject. */
fun parseAppLink(raw: String, host: String): AppLink? =
    (UriDeepLinkMatcher(host).match(raw) as? DeepLinkResult.Matched)?.link
