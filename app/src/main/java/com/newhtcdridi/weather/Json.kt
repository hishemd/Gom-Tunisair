package com.newhtcdridi.weather

import org.json.JSONArray
import org.json.JSONObject

/**
 * Tiny adapter over org.json so the rest of the code (and its unit tests) never touches
 * the Android stubbed classes directly. The real implementation is supplied in tests via
 * `testImplementation("org.json:json:...")`; on the device it maps to the framework's JSONObject.
 */
internal class JsonObject(private val json: JSONObject) {

    fun get(name: String): JsonElement? =
        if (json.has(name) && !json.isNull(name)) JsonElement(json.get(name)) else null

    fun has(name: String): Boolean = json.has(name) && !json.isNull(name)
}

internal class JsonArray(private val json: JSONArray) {
    fun size(): Int = json.length()
    fun get(i: Int): JsonElement? = if (json.isNull(i)) null else JsonElement(json.get(i))
}

internal class JsonElement(private val value: Any?) {

    fun asObject(): JsonObject = JsonObject(value as JSONObject)
    fun asArray(): JsonArray = JsonArray(value as JSONArray)
    fun asString(): String = value as String
    fun asDouble(): Double = (value as Number).toDouble()
    fun asInt(): Int = (value as Number).toInt()
}

/** Parses a JSON document into a [JsonObject] wrapper. */
internal object Json {
    fun parse(text: String): JsonObject = JsonObject(JSONObject(text))
}
