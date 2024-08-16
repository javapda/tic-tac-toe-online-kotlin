package tictactoeonline

import com.auth0.jwt.JWT
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tictactoeonline.util.*
import kotlin.test.assertEquals

class ApplicationTest {
    fun emailFromJwt(jwt: String) =
        JWT.require(algorithm).build().verify(jwt).getClaim("email").asString()

    @BeforeEach
    fun setup() {
        clearAll()
    }

    @Test
    fun `signup two people`() = testApplication {

        // add a person
        var email = "foo@bar.com"
        var password = "foobar"
        var response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val user = User(email = email, password = password)
            val json = Json.encodeToString(user)
            setBody(json)
        }
        val expectedOnFirstSignup = Json.encodeToString(mapOf("status" to Status.SIGNED_UP.message))
        assertEquals(expectedOnFirstSignup, response.bodyAsText())
        assertEquals(1, info().num_users)
        // add another
        email = "foo2@bar.com"
        password = "foo2bar"
        response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val user = User(email = email, password = password)
            val json = Json.encodeToString(user)
            setBody(json)
        }
        val expectedOnSecondSignup = Json.encodeToString(mapOf("status" to Status.SIGNED_UP.message))
        assertEquals(expectedOnSecondSignup, response.bodyAsText())
        assertEquals(2, info().num_users)


    }

    @ParameterizedTest
    @ValueSource(strings = ["carl@example.com:1111", "mike@example.com:2222"])
    fun `signup no auth needed success and failure`(data: String) = testApplication {
        // Successful signup
        val (email, password) = data.trim().split(":")
        var response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val user = User(email = email, password = password)
            val json = Json.encodeToString(user)
            setBody(json)
        }
        assertEquals(Status.SIGNED_UP.statusCode, response.status)
        val expectedOnFirstSignup = Json.encodeToString(mapOf("status" to Status.SIGNED_UP.message))
        assertEquals(expectedOnFirstSignup, response.bodyAsText())
        assertEquals(1, UserStore.size)


        response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val user = User(email = email, password = password)
            val json = Json.encodeToString(user)
            setBody(json)
        }
        val expectedOnSecondSignup = Json.encodeToString(mapOf("status" to Status.REGISTRATION_FAILED.message))
        assertEquals(expectedOnSecondSignup, response.bodyAsText())
        assertEquals(Status.REGISTRATION_FAILED.statusCode, response.status)

        // failed signin / registration due to missing email address
        response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val user = User(email = "", password = password)
            val json = Json.encodeToString(user)
            setBody(json)
        }
        assertEquals(Status.REGISTRATION_FAILED.statusCode, response.status)

        // failed signin / registration due to missing password
        response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val user = User(email = "any@company.com", password = "")
            val json = Json.encodeToString(user)
            setBody(json)
        }
        assertEquals(Status.REGISTRATION_FAILED.statusCode, response.status)

    }

    @Test
    fun `game failed authorization`() = testApplication {
        val response = client.post("/game") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val json = Json.encodeToString(
                NewGameRequestPayload(
                    player1 = "Jed@Clampett.com",
                    player2 = "Wilma@Flintstone.com",
                    size = "4x3"
                )
            )
            setBody(json)
        }
        assertEquals(Status.AUTHORIZATION_FAILED.statusCode, response.status)
        val expectedOnFirstSignup = Json.encodeToString(mapOf("status" to Status.AUTHORIZATION_FAILED.message))
        assertEquals(expectedOnFirstSignup, response.bodyAsText())

    }

    @Test
    fun `game status failed authorization`() = testApplication {
        val game_id = 1
        val response = client.get("/game/$game_id/status") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val json = Json.encodeToString(mapOf<String, String>())
            setBody(json)
        }
        assertEquals(Status.AUTHORIZATION_FAILED.statusCode, response.status)
        val expected = Json.encodeToString(mapOf("status" to Status.AUTHORIZATION_FAILED.message))
        assertEquals(expected, response.bodyAsText())

    }

    @Test
    fun `no authentication GET helloWorld`() = testApplication {
        client.get("/helloWorld").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
            assertEquals(HttpProtocolVersion.HTTP_1_1, version)
            // println("requestTime: $requestTime")
            // println("responseTime: $responseTime")

        }
    }

    @Test
    fun `get help`() = testApplication {
        client.get("/help").apply {
            assertEquals(HttpStatusCode.OK, status)
            val expected = help()
            assertEquals(11, expected.endpoints.size)
            assertEquals(HttpProtocolVersion.HTTP_1_1, version)
        }

    }


    @Test
    fun testjwt() {
        assertEquals(emailCarl, emailFromJwt(jwtCarl))
        assertEquals(emailMike, emailFromJwt(jwtMike))
        assertEquals(emailArtem, emailFromJwt(jwtArtem))
    }

}