package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import java.lang.reflect.Modifier
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

/** docs/openapi.yaml is JSON despite its extension. Field names, paths and query names must match it. */
class OpenApiContractTest {

    private val spec: JsonObject =
        Json.parseToJsonElement(findUp("docs/openapi.yaml").readText(Charsets.UTF_8)).jsonObject

    private val schemas get() = spec.getValue("components").jsonObject.getValue("schemas").jsonObject

    private fun schema(name: String) = schemas.getValue(name).jsonObject

    private fun props(name: String): Set<String> = schema(name).getValue("properties").jsonObject.keys

    private fun names(serializer: KSerializer<*>): Set<String> = serializer.descriptor.elementNames.toSet()

    @Test fun requestFieldNamesMatchSchemas() {
        assertEquals(props("LoginIn"), names(LoginRequest.serializer()))
        assertEquals(props("PinChangeIn"), names(ChangePinRequest.serializer()))
    }

    @Test fun responseFieldNamesMatchSchemas() {
        assertEquals(props("RosterEntry"), names(RosterEntryDto.serializer()))
        assertEquals(props("UserOut"), names(UserDto.serializer()))
        assertEquals(props("LoginOut"), names(LoginResponse.serializer()))
    }

    @Test fun everyPathConstantIsAPathOfTheSpecAndEveryPathHasOne() {
        val constants = ApiConfig.ApiPaths::class.java.declaredFields
            .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .map { it.get(null) as String }
            .filter { it != ApiConfig.ApiPaths.AUTH_PREFIX }
        assertEquals(spec.getValue("paths").jsonObject.keys, constants.toSet())
        assertEquals(constants.size, constants.toSet().size)
    }

    @Test fun loginRoundTripUsesTheSchemaNames() = Harness(session = null).use { h ->
        h.enqueueOk(sample("LoginOut").toString())
        val result = runBlocking { safeApiCall { h.api.login(LoginRequest("12", "9137")) } }
        val session = (result as AppResult.Success).value.toSession()
        assertEquals("v-token", session.token)
        assertEquals("2026-12-22T10:00:00Z", session.expiresAt)
        assertEquals("v-id", session.user.id)
        assertEquals("v-displayName", session.user.displayName)
        assertEquals(Role.FOREMAN, session.user.role)
        assertEquals("ja", session.user.language)
        val sent = Json.parseToJsonElement(h.server.takeRequest().body!!.utf8()).jsonObject
        assertEquals(props("LoginIn"), sent.keys)
    }

    @Test fun rosterQueryNamesMatchTheSpec() = Harness(session = null).use { h ->
        h.enqueueOk(JsonArray(listOf(sample("RosterEntry"))).toString())
        val result = runBlocking { safeApiCall { h.api.roster("SASEBO01", "worker") } }
        assertEquals("v-displayName", (result as AppResult.Success).value.single().displayName)
        val url = h.server.takeRequest().url
        val declared = spec.getValue("paths").jsonObject.getValue(ApiConfig.ApiPaths.ROSTER)
            .jsonObject.getValue("get").jsonObject.getValue("parameters").jsonArray
            .map { it.jsonObject.getValue("name").jsonPrimitive.content }.toSet()
        assertEquals(declared, url.queryParameterNames)
    }

    @Test fun pinChangeBodyUsesTheSchemaNames() = Harness().use { h ->
        h.enqueueOk("null")
        runBlocking { safeApiCallUnit { h.api.changePin(ChangePinRequest("1111", "2222")) } }
        val sent = Json.parseToJsonElement(h.server.takeRequest().body!!.utf8()).jsonObject
        assertEquals(props("PinChangeIn"), sent.keys)
    }

    /** A JSON value for [name] built only from the schema: same keys, first enum value, a date for date-time. */
    private fun sample(name: String): JsonObject = buildJsonObject {
        schema(name).getValue("properties").jsonObject.forEach { (key, def) -> put(key, sampleValue(key, def.jsonObject)) }
    }

    private fun sampleValue(key: String, def: JsonObject): JsonElement {
        def["\$ref"]?.let { return sample(it.jsonPrimitive.content.substringAfterLast('/')) }
        if (def["anyOf"] != null) return JsonNull
        def["enum"]?.let { return it.jsonArray.first() }
        return when (def["type"]?.jsonPrimitive?.content) {
            "boolean" -> JsonPrimitive(true)
            "string" -> JsonPrimitive(if (def["format"]?.jsonPrimitive?.content == "date-time") "2026-12-22T10:00:00Z" else "v-$key")
            else -> JsonNull
        }
    }
}
