package me.emiliomini.dutyschedule.json.util

import com.google.protobuf.Timestamp
import org.json.JSONArray
import org.json.JSONObject

sealed class JsonMapping<T>(vararg val path: String, open val transform: (T) -> T = { it }) {
    class ARRAY(vararg key: String) : JsonMapping<JSONArray>(*key)
    class OBJECT(vararg key: String) : JsonMapping<JSONObject>(*key)
    class BOOLEAN(vararg key: String, override val transform: (Boolean) -> Boolean = { it }) : JsonMapping<Boolean>(*key, transform = transform)
    class INT(vararg key: String, override val transform: (Int) -> Int = { it }) : JsonMapping<Int>(*key, transform = transform)
    class FLOAT(vararg key: String, override val transform: (Float) -> Float = { it }) : JsonMapping<Float>(*key, transform = transform)
    class TIMESTAMP(vararg key: String, override val transform: (Timestamp) -> Timestamp = { it }) : JsonMapping<Timestamp>(*key, transform = transform)
    class STRING(vararg key: String, override val transform: (String) -> String = { it }) : JsonMapping<String>(*key, transform = transform)
}

object JsonMappingPathVariables {
    const val FIRST_KEY = "%FIRST_KEY%"
}