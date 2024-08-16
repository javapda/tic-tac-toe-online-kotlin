package tictactoeonline.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class MyJsonTools {
    companion object {
        fun isJsonObject(text: String): Boolean {
            return text.isNotEmpty() && isJsonParsable(text) &&
                    try {
                        val parsed = Json.parseToJsonElement(text)
                        parsed.jsonObject
                        true
                    } catch (err: Exception) {
                        false
                    }

        }

        fun isNotJsonObject(text: String): Boolean = !isJsonObject(text)

        fun isJsonArray(text: String): Boolean {
            if (isNotJsonParsable(text)) return false
            return try {
                val parsed = Json.parseToJsonElement(text)
                parsed.jsonArray
                true
            } catch (err: Exception) {
                when (err) {
                    is IllegalArgumentException -> false
                    else -> false
                }
            }
        }

        fun isJsonParsable(text: String): Boolean = !isNotJsonParsable(text)
        fun isNotJsonParsable(text: String): Boolean {
            return text.isNotEmpty() && try {
                Json.parseToJsonElement(text)
                false
            } catch (err: Exception) {
                true
            }
        }

        fun jsonObjectGetKeyOrNull(jsonText: String, key: String): Any? {
            if (isNotJsonParsable(jsonText)) throw IllegalArgumentException("not valid json '$jsonText'")

            return if (isJsonObject(jsonText)) {
                val parsed = Json.parseToJsonElement(jsonText)
                val jo = parsed.jsonObject
                val res = jo[key]
                res.toString().replace("^\"|\"$".toRegex(), "")
            } else {
                null
            }
        }

    }
}