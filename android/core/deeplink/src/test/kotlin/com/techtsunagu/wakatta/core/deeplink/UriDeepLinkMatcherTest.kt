package com.techtsunagu.wakatta.core.deeplink

import com.techtsunagu.wakatta.core.deeplink.DeepLinkRejection.DUPLICATE_PARAM
import com.techtsunagu.wakatta.core.deeplink.DeepLinkRejection.ILLEGAL_CHARACTER
import com.techtsunagu.wakatta.core.deeplink.DeepLinkRejection.MALFORMED_ID
import com.techtsunagu.wakatta.core.deeplink.DeepLinkRejection.MALFORMED_PARAM
import com.techtsunagu.wakatta.core.deeplink.DeepLinkRejection.NOT_HTTPS
import com.techtsunagu.wakatta.core.deeplink.DeepLinkRejection.TOO_LONG
import com.techtsunagu.wakatta.core.deeplink.DeepLinkRejection.UNKNOWN_PATH
import com.techtsunagu.wakatta.core.deeplink.DeepLinkRejection.WRONG_HOST
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class UriDeepLinkMatcherTest {

    private val host = "wakatta.example"
    private val matcher = UriDeepLinkMatcher(host)
    private val token = "AbCdEfGhIjKlMnOpQrSt-_" // 22 base64url characters

    private fun assertMatches(raw: String, expected: AppLink) =
        assertEquals(raw, DeepLinkResult.Matched(expected), matcher.match(raw))

    private fun assertRejects(raw: String, reason: DeepLinkRejection) =
        assertEquals(raw.take(80), DeepLinkResult.Rejected(reason), matcher.match(raw))

    @Test
    fun validLinks() {
        val cases = listOf(
            "https://wakatta.example/m/12?s=ABCD1234" to AppLink.Machine("12", "ABCD1234"),
            "https://wakatta.example/m/12" to AppLink.Machine("12", null),
            "https://wakatta.example/m/123456789012345678" to AppLink.Machine("123456789012345678", null),
            "https://wakatta.example/c/$token" to AppLink.Contact(token),
            "https://wakatta.example/t/7" to AppLink.Thread("7", null),
            "https://wakatta.example/t/7?m=99" to AppLink.Thread("7", "99"),
            "https://wakatta.example/n/5" to AppLink.Notice("5", null),
            "https://wakatta.example/n/5?e=3" to AppLink.Notice("5", "3"),
            "https://wakatta.example/b/8" to AppLink.Briefing("8"),
            "https://wakatta.example/nm/4" to AppLink.NearMiss("4"),
            "https://wakatta.example/i/due" to AppLink.InterviewsDue,
        )
        cases.forEach { (raw, expected) -> assertMatches(raw, expected) }
    }

    @Test
    fun toleratedVariants() {
        val cases = listOf(
            "HTTPS://WAKATTA.EXAMPLE/m/12" to AppLink.Machine("12", null), // scheme and host are case-insensitive
            "https://wakatta.example/m/12?utm=x&s=ABCD1234&z" to AppLink.Machine("12", "ABCD1234"), // unknown params ignored
            "https://wakatta.example/m/12?s=ABCD1234#frag" to AppLink.Machine("12", "ABCD1234"), // fragment ignored
            "https://wakatta.example/m/12#?s=EVIL0000" to AppLink.Machine("12", null), // '?' inside a fragment is not a query
            "https://wakatta.example/m/12?" to AppLink.Machine("12", null),
            "https://wakatta.example/m/12?&&s=ABCD1234&" to AppLink.Machine("12", "ABCD1234"),
            "https://wakatta.example/b/8?s=BADSHOP" to AppLink.Briefing("8"), // s is not known on /b/
            "https://wakatta.example/t/7?s=lower&m=1" to AppLink.Thread("7", "1"),
        )
        cases.forEach { (raw, expected) -> assertMatches(raw, expected) }
    }

    @Test
    fun malformedLinks() {
        val cases = listOf(
            "" to NOT_HTTPS,
            "https://" to WRONG_HOST,
            "https://wakatta.example" to UNKNOWN_PATH,
            "https://wakatta.example/" to UNKNOWN_PATH,
            "https://wakatta.example?s=1" to UNKNOWN_PATH,
            "https://wakatta.example/m" to UNKNOWN_PATH,
            "https://wakatta.example/m/" to MALFORMED_ID,
            "https://wakatta.example/m/abc" to MALFORMED_ID,
            "https://wakatta.example/m/-1" to MALFORMED_ID,
            "https://wakatta.example/m/1.5" to MALFORMED_ID,
            "https://wakatta.example/m/1234567890123456789" to MALFORMED_ID, // 19 digits
            "https://wakatta.example/m/12/" to UNKNOWN_PATH,
            "https://wakatta.example/m/12/extra" to UNKNOWN_PATH,
            "https://wakatta.example//m/12" to UNKNOWN_PATH,
            "https://wakatta.example/M/12" to UNKNOWN_PATH,
            "https://wakatta.example/m/12?s=abcd1234" to MALFORMED_PARAM, // lower case
            "https://wakatta.example/m/12?s=ABC" to MALFORMED_PARAM,
            "https://wakatta.example/m/12?s=ABCD12345" to MALFORMED_PARAM,
            "https://wakatta.example/m/12?s=" to MALFORMED_PARAM,
            "https://wakatta.example/m/12?s" to MALFORMED_PARAM,
            "https://wakatta.example/m/12?s=ABCD1234&s=EFGH5678" to DUPLICATE_PARAM,
            "https://wakatta.example/t/7?m=x" to MALFORMED_PARAM,
            "https://wakatta.example/n/5?e=" to MALFORMED_PARAM,
            "https://wakatta.example/c/short" to MALFORMED_ID,
            "https://wakatta.example/c/${token}A" to MALFORMED_ID, // 23 characters
            "https://wakatta.example/c/AbCdEfGhIjKlMnOpQrSt-." to MALFORMED_ID,
            "https://wakatta.example/c/AbCdEfGhIjKlMnOpQrSt+/" to UNKNOWN_PATH,
            "https://wakatta.example/i/" to UNKNOWN_PATH,
            "https://wakatta.example/i/soon" to UNKNOWN_PATH,
            "https://wakatta.example/i/due/" to UNKNOWN_PATH,
            "https://wakatta.example/i" to UNKNOWN_PATH,
            "https://wakatta.example/senpai/abc" to UNKNOWN_PATH, // web only
            "https://wakatta.example/panel/1" to UNKNOWN_PATH,
            "https://wakatta.example/me" to UNKNOWN_PATH,
            "https://wakatta.example/.well-known/assetlinks.json" to UNKNOWN_PATH,
        )
        cases.forEach { (raw, reason) -> assertRejects(raw, reason) }
    }

    @Test
    fun hostileSchemeAndHost() {
        val cases = listOf(
            "http://wakatta.example/m/1" to NOT_HTTPS,
            "ftp://wakatta.example/m/1" to NOT_HTTPS,
            "javascript:alert(1)" to NOT_HTTPS,
            "intent://wakatta.example/m/1#Intent;scheme=https;end" to NOT_HTTPS,
            "wakatta://m/1" to NOT_HTTPS,
            "wk:u_rahim" to NOT_HTTPS,
            "ABCD1234" to NOT_HTTPS,
            "//wakatta.example/m/1" to NOT_HTTPS,
            "https:wakatta.example/m/1" to NOT_HTTPS,
            "https:/wakatta.example/m/1" to NOT_HTTPS,
            "https://example.com/m/1" to WRONG_HOST,
            "https://wakatta.example.evil.com/m/1" to WRONG_HOST,
            "https://evil.com/wakatta.example/m/1" to WRONG_HOST,
            "https://evilwakatta.example/m/1" to WRONG_HOST,
            "https://sub.wakatta.example/m/1" to WRONG_HOST,
            "https://wakatta.example./m/1" to WRONG_HOST, // trailing dot
            "https://user@wakatta.example/m/1" to WRONG_HOST,
            "https://user:pw@wakatta.example/m/1" to WRONG_HOST,
            "https://wakatta.example@evil.com/m/1" to WRONG_HOST,
            "https://evil.com@wakatta.example/m/1" to WRONG_HOST,
            "https://wakatta.example:443/m/1" to WRONG_HOST,
            "https://wakatta.example:8080/m/1" to WRONG_HOST,
            "https://wakatta.example:@evil.com/m/1" to WRONG_HOST,
            "https://wakatta.example\\@evil.com/m/1" to WRONG_HOST, // backslash joins the host in some parsers
            "https://[wakatta.example]/m/1" to WRONG_HOST,
            "https://[HOST]/m/1" to WRONG_HOST, // an unreplaced placeholder never matches
            "https://127.0.0.1/m/1" to WRONG_HOST,
        )
        cases.forEach { (raw, reason) -> assertRejects(raw, reason) }
    }

    @Test
    fun hostileOpenRedirect() {
        // No param is ever followed, so these are plain bad values or unknown keys.
        assertMatches("https://wakatta.example/m/1?next=https://evil.com", AppLink.Machine("1", null))
        assertMatches("https://wakatta.example/m/1?redirect=//evil.com&s=ABCD1234", AppLink.Machine("1", "ABCD1234"))
        assertRejects("https://wakatta.example/m/1?s=https://evil.com", MALFORMED_PARAM)
        assertRejects("https://wakatta.example/t/1?m=https://evil.com", MALFORMED_PARAM)
        assertRejects("https://wakatta.example/redirect?url=https://evil.com", UNKNOWN_PATH)
        assertRejects("https://wakatta.example/m/https://evil.com", UNKNOWN_PATH)
    }

    @Test
    fun hostilePathTraversalAndEncoding() {
        val cases = listOf(
            "https://wakatta.example/m/../c/$token" to UNKNOWN_PATH,
            "https://wakatta.example/m/12/../13" to UNKNOWN_PATH,
            "https://wakatta.example/m/./12" to UNKNOWN_PATH,
            "https://wakatta.example/m/..%2F12" to MALFORMED_ID,
            "https://wakatta.example/m/%2e%2e/12" to UNKNOWN_PATH,
            "https://wakatta.example/m/1%2F2" to MALFORMED_ID,
            "https://wakatta.example/m/1%32" to MALFORMED_ID, // percent-encoded digit is not decoded
            "https://wakatta.example/m/12%00" to MALFORMED_ID,
            "https://wakatta.example/%6d/12" to UNKNOWN_PATH, // encoded path letter
            "https://wakatta.example/m%2F12" to UNKNOWN_PATH,
            "https://wakatta.example/m\\12" to UNKNOWN_PATH,
            "https://wakatta.example/m/12\\..\\13" to MALFORMED_ID,
            "https://wakatta.example/m/12;jsessionid=1" to MALFORMED_ID,
            "https://wakatta.example/m/12?s=ABCD123%31" to MALFORMED_PARAM, // encoded value is not decoded
            "https://wakatta.example/m/12?s=ABCD12%334" to MALFORMED_PARAM,
            "https://wakatta.example/c/AbCdEfGhIjKlMnOpQr%53t-_" to MALFORMED_ID,
        )
        cases.forEach { (raw, reason) -> assertRejects(raw, reason) }
        // An encoded key is a different (unknown) key, so the real s is still absent.
        assertMatches("https://wakatta.example/m/12?%73=ABCD1234", AppLink.Machine("12", null))
    }

    @Test
    fun hostileUnicodeAndControlCharacters() {
        val cases = listOf(
            "https://wakatta.example/m/١٢" to ILLEGAL_CHARACTER, // Arabic-Indic digits
            "https://wakatta.example/m/１２" to ILLEGAL_CHARACTER, // full-width digits
            "https://wakatta.example/m/12​" to ILLEGAL_CHARACTER, // zero width space
            "https://wakatta.example/m/12‮" to ILLEGAL_CHARACTER, // right-to-left override
            "https://wakatta.example/m/12 " to ILLEGAL_CHARACTER, // no-break space
            "https://wakatta.example/m/12﻿" to ILLEGAL_CHARACTER, // BOM
            "https://wаkatta.example/m/12" to ILLEGAL_CHARACTER, // Cyrillic a in the host
            "https://wakatta.example/m/12?s=ＡＢＣＤ１２３４" to ILLEGAL_CHARACTER, // full-width shop code
            "https://wakatta．example/m/12" to ILLEGAL_CHARACTER, // ideographic full stop
            "https://wakatta.example／m/12" to ILLEGAL_CHARACTER, // full-width slash
            "https://wakatta.example/m/12\n" to ILLEGAL_CHARACTER,
            "https://wakatta.example/m/12\r\nHost: evil.com" to ILLEGAL_CHARACTER,
            "https://wakatta.example/m/12\u0000" to ILLEGAL_CHARACTER,
            " https://wakatta.example/m/12" to ILLEGAL_CHARACTER,
            "https://wakatta.example/m/12 " to ILLEGAL_CHARACTER,
            "https://wakatta.example/m/1 2" to ILLEGAL_CHARACTER,
            "https://wakatta.example/m/12\u007F" to ILLEGAL_CHARACTER,
            "wk:ユーザー" to ILLEGAL_CHARACTER,
        )
        cases.forEach { (raw, reason) -> assertRejects(raw, reason) }
    }

    @Test
    fun hostileSizes() {
        assertRejects("https://wakatta.example/m/" + "9".repeat(5000), TOO_LONG)
        assertRejects("a".repeat(5000), TOO_LONG)
        assertRejects("https://wakatta.example/m/12?x=" + "a".repeat(2048), TOO_LONG)
        assertRejects("https://wakatta.example/m/" + "1".repeat(19), MALFORMED_ID)
        // Exactly at the cap is parsed, not refused for size.
        val atCap = "https://wakatta.example/m/12?x=" + "a".repeat(UriDeepLinkMatcher.MAX_LENGTH - 31)
        assertEquals(UriDeepLinkMatcher.MAX_LENGTH, atCap.length)
        assertMatches(atCap, AppLink.Machine("12", null))
        // Many params must not slow or break parsing.
        assertMatches("https://wakatta.example/m/12?" + "a=1&".repeat(400) + "s=ABCD1234", AppLink.Machine("12", "ABCD1234"))
    }

    @Test
    fun scannerAcceptsExactlyTheThreeContractShapes() {
        assertMatches("https://wakatta.example/m/12?s=ABCD1234", AppLink.Machine("12", "ABCD1234"))
        assertMatches("https://wakatta.example/m/12", AppLink.Machine("12", null))
        assertMatches("https://wakatta.example/c/$token", AppLink.Contact(token))
        listOf("http://wakatta.example/m/1", "https://wakatta.example.evil.com/m/1", "https://user@wakatta.example/m/1", "wk:u_rahim", "/m/abc", "/c/short", "/senpai/x", "", "x".repeat(5000))
            .forEach { assertNull(it.take(40), (matcher.match(it) as? DeepLinkResult.Matched)) }
    }

    @Test
    fun notificationPayload() {
        val link = "https://wakatta.example/t/7"
        assertEquals(
            DeepLinkResult.Matched(AppLink.Thread("7", null)),
            matcher.matchNotification(mapOf("v" to "1", "type" to "chat_message", "link" to link, "extra" to "x")),
        )
        assertEquals(DeepLinkResult.Rejected(DeepLinkRejection.UNSUPPORTED_VERSION), matcher.matchNotification(mapOf("link" to link)))
        assertEquals(DeepLinkResult.Rejected(DeepLinkRejection.UNSUPPORTED_VERSION), matcher.matchNotification(mapOf("v" to "2", "link" to link)))
        assertEquals(DeepLinkResult.Rejected(DeepLinkRejection.MISSING_LINK), matcher.matchNotification(mapOf("v" to "1")))
        assertEquals(DeepLinkResult.Rejected(WRONG_HOST), matcher.matchNotification(mapOf("v" to "1", "link" to "https://evil.com/t/7")))
        // The link is the only navigation input: ids in other keys are ignored.
        assertEquals(DeepLinkResult.Rejected(DeepLinkRejection.MISSING_LINK), matcher.matchNotification(mapOf("v" to "1", "threadId" to "7")))
    }

    @Test
    fun everyNotificationLinkInTheContractMatches() {
        val links = listOf(
            "https://wakatta.example/t/7",
            "https://wakatta.example/m/3?s=ABCD1234",
            "https://wakatta.example/t/7?m=12",
            "https://wakatta.example/b/2",
            "https://wakatta.example/n/9",
            "https://wakatta.example/n/9?e=4",
            "https://wakatta.example/i/due",
            "https://wakatta.example/nm/6",
        )
        links.forEach {
            assertEquals(it, DeepLinkResult.Matched::class, matcher.matchNotification(mapOf("v" to "1", "link" to it))::class)
        }
    }

    @Test
    fun severalHostsForStagingBuilds() {
        val two = UriDeepLinkMatcher(setOf("wakatta.example", "Staging.Wakatta.Example"))
        assertEquals(DeepLinkResult.Matched(AppLink.Briefing("1")), two.match("https://staging.wakatta.example/b/1"))
        assertEquals(DeepLinkResult.Matched(AppLink.Briefing("1")), two.match("https://wakatta.example/b/1"))
        assertEquals(DeepLinkResult.Rejected(WRONG_HOST), two.match("https://other.example/b/1"))
    }

    @Test
    fun badHostConfigurationFailsFast() {
        listOf("[HOST]", "", "https://wakatta.example", "wakatta.example/", "wakatta.example:443", "wakatta.example.", "wаkatta.example", "a b")
            .forEach { bad ->
                try {
                    UriDeepLinkMatcher(bad)
                    fail("accepted host: $bad")
                } catch (expected: IllegalArgumentException) {
                    // expected
                }
            }
        try {
            UriDeepLinkMatcher(emptySet())
            fail("accepted empty host set")
        } catch (expected: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun parseAppLinkMatchesContractSignature() {
        assertEquals(AppLink.Machine("12", "ABCD1234"), parseAppLink("https://wakatta.example/m/12?s=ABCD1234", "wakatta.example"))
        assertNull(parseAppLink("https://evil.com/m/12", "wakatta.example"))
    }

    @Test
    fun pendingLinkSurvivesSerialization() {
        val json = Json
        listOf<AppLink>(
            AppLink.Machine("12", "ABCD1234"),
            AppLink.Machine("12", null),
            AppLink.Contact(token),
            AppLink.Thread("7", "9"),
            AppLink.Notice("5", null),
            AppLink.Briefing("8"),
            AppLink.NearMiss("4"),
            AppLink.InterviewsDue,
        ).forEach {
            assertEquals(it, json.decodeFromString(AppLink.serializer(), json.encodeToString(AppLink.serializer(), it)))
        }
    }
}
