package me.emiliomini.dutyschedule.json.util

import com.google.protobuf.Timestamp
import org.json.JSONArray
import org.json.JSONObject

sealed class JsonMapping<T>(vararg val path: String) {
    class ARRAY(vararg key: String) : JsonMapping<JSONArray>(*key)
    class OBJECT(vararg key: String) : JsonMapping<JSONObject>(*key)
    class BOOLEAN(vararg key: String) : JsonMapping<Boolean>(*key)
    class INT(vararg key: String) : JsonMapping<Int>(*key)
    class FLOAT(vararg key: String) : JsonMapping<Float>(*key)
    class TIMESTAMP(vararg key: String) : JsonMapping<Timestamp>(*key)
    class STRING(vararg key: String) : JsonMapping<String>(*key)
}

object JsonMappingPathVariables {
    val FIRST_KEY = "%FIRST_KEY%"
}