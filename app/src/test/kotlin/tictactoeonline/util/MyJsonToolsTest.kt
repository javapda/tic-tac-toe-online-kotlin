package tictactoeonline.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MyJsonToolsTest {

    @ParameterizedTest
    @ValueSource(strings = ["""{"status":"some-status-here"}"""])
    fun `test isJsonObject`(data: String) {
        assertTrue(MyJsonTools.isJsonObject(data))
    }

    @ParameterizedTest
    @ValueSource(strings = ["""{""", "hello"])
    fun `test isNotJsonObject`(data: String) {
        assertTrue(MyJsonTools.isNotJsonObject(data))
    }

    @ParameterizedTest
    @ValueSource(strings = ["""[{"status":23}]""", """[{"status":23,"happy":true}]"""])
    fun `test isJsonArray`(jsonTextCandidate: String) {
        assertTrue(MyJsonTools.isJsonArray(jsonTextCandidate))
    }

    @ParameterizedTest
    @ValueSource(strings = ["{JED", """[{"status":23},BAD:]"""])
    fun `test isNotJsonParsable`(jsonCandidateText: String) {
        assertTrue(MyJsonTools.isNotJsonParsable(jsonCandidateText))
    }

    @ParameterizedTest
    @ValueSource(strings = ["""{"status":23}~status~23""", """{"status":"John here"}~status~John here"""])
    fun `test jsonObjectGetKeyOrNull`(data: String) {
        val (jsonText, key, expected) = data.split("~")
        assertEquals(expected, MyJsonTools.jsonObjectGetKeyOrNull(jsonText, key))
    }
}