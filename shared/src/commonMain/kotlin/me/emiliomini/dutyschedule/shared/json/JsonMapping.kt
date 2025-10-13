@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.emiliomini.dutyschedule.shared.datastores.Timestamp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed class JsonMapping<T>(vararg val path: String, open val transform: (T) -> T = { it }) {
    class ARRAY(vararg key: String) : JsonMapping<JsonArray>(*key)
    class OBJECT(vararg key: String) : JsonMapping<JsonObject>(*key)
    class ARRAY_OR_OBJECT(vararg key: String) : JsonMapping<JsonElement>(*key)
    class BOOLEAN(vararg key: String, override val transform: (Boolean) -> Boolean = { it }) : JsonMapping<Boolean>(*key, transform = transform)
    class INT(vararg key: String, override val transform: (Int) -> Int = { it }) : JsonMapping<Int>(*key, transform = transform)
    class FLOAT(vararg key: String, override val transform: (Float) -> Float = { it }) : JsonMapping<Float>(*key, transform = transform)
    class TIMESTAMP(vararg key: String, override val transform: (Timestamp) -> Timestamp = { it }) : JsonMapping<Timestamp>(*key, transform = transform)
    class INSTANT(vararg key: String, override val transform: (Instant) -> Instant = { it }) : JsonMapping<Instant>(*key, transform = transform)
    class STRING(vararg key: String, override val transform: (String) -> String = { it }) : JsonMapping<String>(*key, transform = transform)
}

object JsonMappingPathVariables {
    const val FIRST_KEY = "%FIRST_KEY%"
}