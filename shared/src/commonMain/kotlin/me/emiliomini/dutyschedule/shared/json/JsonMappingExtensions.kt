@file:Suppress("UNCHECKED_CAST")

package me.emiliomini.dutyschedule.shared.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.emiliomini.dutyschedule.shared.api.getPlatformLogger
import me.emiliomini.dutyschedule.shared.util.toInstant
import me.emiliomini.dutyschedule.shared.util.toTimestamp
import kotlin.time.ExperimentalTime

private val logger = getPlatformLogger("JSON")

data class JsonPositionedObject(val key: String, val o: JsonElement)
data class JsonPositionedObjectSkippable(val key: String, val o: JsonElement, val skip: () -> Unit)

fun JsonElement?.isJsonObject(): Boolean {
    if (this == null) {
        return false
    }

    try {
        this.jsonObject
        return true
    } catch (_: IllegalArgumentException) {
        return false
    }
}

fun JsonElement?.isJsonPrimitive(): Boolean {
    if (this == null) {
        return false
    }

    try {
        this.jsonPrimitive
        return true
    } catch (_: IllegalArgumentException) {
        return false
    }
}

fun JsonElement?.isJsonArray(): Boolean {
    if (this == null) {
        return false
    }

    try {
        this.jsonArray
        return true
    } catch (_: IllegalArgumentException) {
        return false
    }
}

fun <T> JsonElement.value(m: JsonMapping<T>): T? {
    if (m.path.isEmpty() || !this.isJsonObject()) {
        return null
    }

    var obj = this.jsonObject
    for (i in m.path.indices) {
        val key = when (m.path[i]) {
            JsonMappingPathVariables.FIRST_KEY -> obj.keys.asSequence().firstOrNull()
            else -> m.path[i]
        }

        if (key == null) {
            return null
        }

        if (i == m.path.lastIndex) {
            val result = obj.valueBySingleKey(m, key)
            return if (result == null) null else m.transform(result)
        }

        obj = try {
            val element = obj[key]
            if (element == null) {
                throw IllegalArgumentException()
            }
            element.jsonObject
        } catch (e: IllegalArgumentException) {
            logger.w(
                "Failed to get nested object with key $key in mapping ${m.path.joinToString(";")}",
                e
            )
            return null
        }
    }

    return null
}

@OptIn(ExperimentalTime::class)
private fun <T> JsonObject.valueBySingleKey(mapping: JsonMapping<T>, key: String): T? {
    if (!this.containsKey(key)) {
        return null
    }

    return try {
        when (mapping) {
            is JsonMapping.ARRAY -> this[key]!!.jsonArray
            is JsonMapping.OBJECT -> this[key]!!.jsonObject
            is JsonMapping.BOOLEAN -> this[key]!!.jsonPrimitive.booleanOrNull
            is JsonMapping.INT -> this[key]!!.jsonPrimitive.intOrNull
            is JsonMapping.FLOAT -> this[key]!!.jsonPrimitive.floatOrNull
            is JsonMapping.TIMESTAMP -> this[key]!!.jsonPrimitive.toString().toInstant()
                .toTimestamp()

            is JsonMapping.INSTANT -> this[key]!!.jsonPrimitive.toString().toInstant()
            is JsonMapping.STRING -> this[key]!!.jsonPrimitive.toString()
        } as T
    } catch (e: IllegalArgumentException) {
        logger.w("Failed to map value for key $key in mapping ${mapping.path.joinToString(";")}", e)
        null
    }
}

fun JsonElement.forEachElement(action: (JsonPositionedObject) -> Unit) {
    if (this.isJsonObject()) {
        val obj = this.jsonObject
        for (key in obj.keys) {
            try {
                val obj = obj[key]
                if (obj == null) {
                    throw IllegalArgumentException()
                }
                action(JsonPositionedObject(key, obj))
            } catch (e: IllegalArgumentException) {
                logger.w("Object ForEach skipped an object", e)
                continue
            }
        }
    } else if (this.isJsonArray()) {
        val array = this.jsonArray
        for (i in 0 until array.size) {
            try {
                val obj = array[i]
                action(JsonPositionedObject("$i", obj))
            } catch (e: IllegalArgumentException) {
                logger.w("Array ForEach skipped an object", e)
                continue
            }
        }
    }
}

fun <T> JsonElement.mapElements(transform: (JsonPositionedObjectSkippable) -> T): List<T> {
    val result = mutableListOf<T>()
    this.forEachElement {
        try {
            var shouldSkip = false
            val resultValue =
                transform(JsonPositionedObjectSkippable(it.key, it.o) { shouldSkip = true })
            if (shouldSkip) {
                return@forEachElement
            }
            result.add(resultValue)
        } catch (e: IllegalArgumentException) {
            logger.w("List mapping skipped an object", e)
            return@forEachElement
        }
    }
    return result
}

fun <K, V> JsonElement.mapElements(
    key: (JsonPositionedObjectSkippable) -> K?,
    value: (JsonPositionedObjectSkippable) -> V
): Map<K, V> {
    val result = mutableMapOf<K, V>()
    this.forEachElement {
        try {
            var shouldSkip = false
            val resultKey = key(JsonPositionedObjectSkippable(it.key, it.o) { shouldSkip = true })
            if (resultKey == null || shouldSkip) {
                return@forEachElement
            }
            val resultValue = value(JsonPositionedObjectSkippable(it.key, it.o) { shouldSkip = true })
            if (shouldSkip) {
                return@forEachElement
            }
            result.put(resultKey, resultValue)
        } catch (e: IllegalArgumentException) {
            logger.w("Map mapping skipped an object", e)
            return@forEachElement
        }
    }
    return result
}