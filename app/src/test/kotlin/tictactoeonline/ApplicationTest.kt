package tictactoeonline

import com.auth0.jwt.JWT
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals

class ApplicationTest {

    @BeforeEach
    fun setup() {
        clearAll()
    }

    @Test
    fun `play a game`() {

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
    fun `game move failed authorization`() = testApplication {
        val game_id = 1
        val response = client.post("/game/$game_id/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val json = Json.encodeToString(
                PlayerMoveRequestPayload(move = "(1,1)")
            )
            setBody(json)
        }
        assertEquals(Status.AUTHORIZATION_FAILED.statusCode, response.status)
        val expected = Json.encodeToString(mapOf("status" to Status.AUTHORIZATION_FAILED.message))
        assertEquals(expected, response.bodyAsText())

    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `signup and signin two people`() = testApplication {
        lateinit var user1: User
        lateinit var user2: User
        lateinit var response: HttpResponse
        // add a person
        var email1 = "foo1@bar.com"
        var password1 = "1111"
        var email2 = "foo2@bar.com"
        var password2 = "2222"

        // fail /game first
        response = client.post("/game") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            user1 = User(email = email1, password = password1)
            val json = Json.encodeToString(user1)
            setBody(json)
        }
        val ma: Map<String, String> = Json.decodeFromString<Map<String, String>>(response.bodyAsText())
        assertEquals(Status.AUTHORIZATION_FAILED.statusCode, response.status)
        assertEquals(Status.AUTHORIZATION_FAILED.message, ma["status"])

        // signup Player 1
        response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            user1 = User(email = email1, password = password1)
            val json = Json.encodeToString(user1)
            setBody(json)
        }
        val expectedOnFirstSignup = Json.encodeToString(mapOf("status" to Status.SIGNED_UP.message))
        assertEquals(expectedOnFirstSignup, response.bodyAsText())
        assertEquals(1, info().num_users)

        // signup Player 2
        response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            user2 = User(email = email2, password = password2)
            val json = Json.encodeToString(user2)
            setBody(json)
        }
        val expectedOnSecondSignup = Json.encodeToString(mapOf("status" to Status.SIGNED_UP.message))
        assertEquals(expectedOnSecondSignup, response.bodyAsText())
        assertEquals(2, info().num_users)

        // signin Player 1
        response = client.post("/signin") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val json = Json.encodeToString(user1)
            setBody(json)
        }
        val bodyJson = response.bodyAsText()

        var playerSigninResponsePayload: PlayerSigninResponsePayload = Json.decodeFromString(bodyJson)
        user1.jwt = playerSigninResponsePayload.token
        assertEquals(Status.SIGNED_IN.message, playerSigninResponsePayload.status)
        println(playerSigninResponsePayload)
        assertEquals(1, UserSignedInStore.size)
        assertEquals(1, info().num_users_signin)

        // signin Player 2
        response = client.post("/signin") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val json = Json.encodeToString(user2)
            setBody(json)
        }
        playerSigninResponsePayload = Json.decodeFromString(response.bodyAsText())
        user2.jwt = playerSigninResponsePayload.token
        assertEquals(Status.SIGNED_IN.message, playerSigninResponsePayload.status)
        println(playerSigninResponsePayload)
        assertEquals(2, UserSignedInStore.size)
        assertEquals(2, info().num_users_signin)

        // successfully /game first
        response = client.post("/game") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
//            println(
//                """
//                user1.jwt:  ${user1.jwt}
//            """.trimIndent()
//            )
//            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val payloadOfJwt:String = JWT().decodeJwt(user1.jwt).payload
            val contents = String(Base64.decode(payloadOfJwt))
            @Serializable
            data class Payload(val email:String)
            val payload = Json.decodeFromString<Payload>(contents)
            val email = payload.email
            println("""
                ${"+".repeat(80)}
                jwt: ${user1.jwt}
                payload: $payload
                contents:  $contents
                email:  $email
                ${"+".repeat(80)}
                
            """.trimIndent())
            header("Authorization", "Bearer ${user1.jwt}")
            header("Monkey", "not-a-gorilla")

            val ngr: NewGameRequestPayload =
                NewGameRequestPayload(player1 = user1.email, player2 = "", size = "4x3")
            val json = Json.encodeToString(ngr)
            setBody(json)
        }
        val bodyJsonHere = response.bodyAsText()
        val ngr: NewGameResponsePayload = Json.decodeFromString<NewGameResponsePayload>(bodyJsonHere)
        assertEquals(Status.NEW_GAME_STARTED.statusCode, response.status)
        assertEquals(Status.NEW_GAME_STARTED.message, ngr.status)
        assertEquals(1, ngr.gameId)


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
    fun `no authentication GET helloWorld`() = testApplication {
        client.get("/helloWorld").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
            assertEquals(HttpProtocolVersion.HTTP_1_1, version)
            println("requestTime: $requestTime")
            println("responseTime: $responseTime")

        }
    }

    @Test
    fun `get help`() = testApplication {
        client.get("/help").apply {
            assertEquals(HttpStatusCode.OK, status)
            val expected = help()
            assertEquals(10, expected.endpoints.size)
            assertEquals(HttpProtocolVersion.HTTP_1_1, version)
        }

    }


}