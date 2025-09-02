@file:Suppress("UNCHECKED_CAST")

package me.emiliomini.dutyschedule.json.util

import android.util.Log
import me.emiliomini.dutyschedule.util.toTimestamp
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.iterator

private val TAG = "JSON"

data class JsonIndexedObject(val index: Int, val o: JSONObject)
data class JsonKeyedObject(val key: String, val o: JSONObject)
data class JsonIndexedObjectSkippable(val index: Int, val o: JSONObject, val skip: () -> Unit)
data class JsonKeyedObjectSkippable(val key: String, val o: JSONObject, val skip: () -> Unit)

fun <T> JSONObject.value(mapping: JsonMapping<T>): T? {
    if (mapping.path.isEmpty()) {
        return null
    }

    var obj = this
    for (i in mapping.path.indices) {
        val key = when (mapping.path[i]) {
            JsonMappingPathVariables.FIRST_KEY -> obj.keys().asSequence().first()
            else -> mapping.path[i]
        }

        if (i == mapping.path.lastIndex) {
            return obj.valueBySingleKey(mapping, key)
        }

        obj = obj.getJSONObject(key)
    }

    return null
}

private fun <T> JSONObject.valueBySingleKey(mapping: JsonMapping<T>, key: String): T? {
    if (!this.has(key)) {
        return null
    }

    return when (mapping) {
        is JsonMapping.ARRAY -> this.getJSONArray(key)
        is JsonMapping.OBJECT -> this.getJSONObject(key)
        is JsonMapping.BOOLEAN -> this.getBoolean(key)
        is JsonMapping.INT -> this.getInt(key)
        is JsonMapping.FLOAT -> this.getDouble(key).toFloat()
        is JsonMapping.TIMESTAMP -> this.getString(key).toTimestamp()
        is JsonMapping.STRING -> this.getString(key)
    } as T
}

fun JSONArray.forEach(action: (JsonIndexedObject) -> Unit) {
    for (i in 0 until this.length()) {
        try {
            val obj = this.getJSONObject(i)
            action(JsonIndexedObject(i, obj))
        } catch (e: JSONException) {
            Log.w(TAG, "Array ForEach skipped an object", e)
            continue
        }
    }
}

fun <T> JSONArray.map(transform: (JsonIndexedObjectSkippable) -> T): List<T> {
    val result = mutableListOf<T>()
    this.forEach {
        try {
            var shouldSkip = false
            val resultValue = transform(JsonIndexedObjectSkippable(it.index, it.o) { shouldSkip = true })
            if (shouldSkip) {
                return@forEach
            }
            result.add(resultValue)
        } catch (e: JSONException) {
            Log.w(TAG, "List mapping skipped an object", e)
            return@forEach
        }
    }
    return result
}

fun <K, V> JSONArray.map(key: (JsonIndexedObjectSkippable) -> K?, value: (JsonIndexedObjectSkippable) -> V): Map<K, V> {
    val result = mutableMapOf<K, V>()
    this.forEach {
        try {
            var shouldSkip = false
            val resultKey = key(JsonIndexedObjectSkippable(it.index, it.o) { shouldSkip = true })
            if (resultKey == null || shouldSkip) {
                return@forEach
            }
            val resultValue = value(JsonIndexedObjectSkippable(it.index, it.o) { shouldSkip = true })
            if (shouldSkip) {
                return@forEach
            }
            result.put(resultKey, resultValue)
        } catch (e: JSONException) {
            Log.w(TAG, "Map mapping skipped an object", e)
            return@forEach
        }
    }
    return result
}

fun JSONObject.forEach(action: (JsonKeyedObject) -> Unit) {
    for (key in this.keys()) {
        try {
            val obj = this.getJSONObject(key)
            action(JsonKeyedObject(key, obj))
        } catch (e: JSONException) {
            Log.w(TAG, "Object ForEach skipped an object", e)
            continue
        }
    }
}

fun <T> JSONObject.map(transform: (JsonKeyedObjectSkippable) -> T): List<T> {
    val result = mutableListOf<T>()
    this.forEach {
        try {
            var shouldSkip = false
            val resultValue = transform(JsonKeyedObjectSkippable(it.key, it.o) { shouldSkip = true })
            if (shouldSkip) {
                return@forEach
            }
            result.add(resultValue)
        } catch (e: JSONException) {
            Log.w(TAG, "List mapping skipped an object", e)
            return@forEach
        }
    }
    return result
}

fun <K, V> JSONObject.map(key: (JsonKeyedObjectSkippable) -> K?, value: (JsonKeyedObjectSkippable) -> V): Map<K, V> {
    val result = mutableMapOf<K, V>()
    this.forEach {
        try {
            var shouldSkip = false
            val resultKey = key(JsonKeyedObjectSkippable(it.key, it.o) { shouldSkip = true })
            if (resultKey == null || shouldSkip) {
                return@forEach
            }
            val resultValue = value(JsonKeyedObjectSkippable(it.key, it.o) { shouldSkip = true })
            if (shouldSkip) {
                return@forEach
            }
            result.put(resultKey, resultValue)
        } catch (e: JSONException) {
            Log.w(TAG, "Map mapping skipped an object", e)
            return@forEach
        }
    }
    return result
}