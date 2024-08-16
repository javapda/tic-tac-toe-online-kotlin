package tictactoeonline.hyperskill

import com.auth0.jwt.JWT
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tictactoeonline.*
import tictactoeonline.util.algorithm
import tictactoeonline.util.emailArtem
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HyperskillStage3Example2Test {
    fun emailFromJwt(jwt: String) =
        JWT.require(algorithm).build().verify(jwt).getClaim("email").asString()

    @BeforeEach
    fun setup() {
        clearAll()
    }

    @Test
    fun `Example 2`() = testApplication {
        lateinit var user1: User
        lateinit var user2: User
        lateinit var response: HttpResponse
        val example2Size = "3x3"

        // 1. Request: POST /signup
        // signup Artem without password - failure
        response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            user1 = User(email = emailArtem, password = "")
            val json = Json.encodeToString(user1)
            setBody(json)
        }
        val expectedOnFirstSignup = Json.encodeToString(mapOf("status" to Status.REGISTRATION_FAILED.message))
        assertEquals(expectedOnFirstSignup, response.bodyAsText())
        assertEquals(0, info().num_users)

        // 2. Request: POST /signup
        // signup Artem with email + password - Success
        response = client.post("/signup") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            user1 = User(email = emailArtem, password = "1234")
            val json = Json.encodeToString(user1)
            setBody(json)
        }
        assertEquals(Status.SIGNED_UP.statusCode, response.status)
        assertEquals(
            Status.SIGNED_UP.message,
            Json.decodeFromString<Map<String, String>>(response.bodyAsText())["status"]
        )
        assertEquals(1, info().num_users)

        // 3. Request: POST /signin
        // signin Artem with incorrect password
        response = client.post("/signin") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val json = Json.encodeToString(user1)
            setBody(json)
        }

        var playerSigninResponsePayload: PlayerSigninResponsePayload =
            Json.decodeFromString<PlayerSigninResponsePayload>(response.bodyAsText())
        user1.jwt = playerSigninResponsePayload.token
        assertEquals(Status.SIGNED_IN.statusCode, response.status)
        assertEquals(Status.SIGNED_IN.message, playerSigninResponsePayload.status)
        assertEquals(1, UserSignedInStore.size)
        assertEquals(1, info().num_users_signin)

        // 4. Request: POST /signin
        // signing Artem (Player1)
        response = client.post("/signin") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val json = Json.encodeToString(user1)
            setBody(json)
        }
        playerSigninResponsePayload = Json.decodeFromString<PlayerSigninResponsePayload>(response.bodyAsText())
        assertEquals(Status.SIGNED_IN.message, playerSigninResponsePayload.status)
        assertEquals(1, UserSignedInStore.size)
        assertEquals(1, info().num_users_signin)

        // 5. Request: POST /game
        // auth Artem start a game as Player2
        response = client.post("/game") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")

            val email = emailFromJwt(user1.jwt!!)
            val ngr: NewGameRequestPayload =
                NewGameRequestPayload(player1 = "", player2 = user1.email, size = example2Size)
            val json = Json.encodeToString(ngr)
            setBody(json)
        }
        val ngr: NewGameResponsePayload = Json.decodeFromString<NewGameResponsePayload>(response.bodyAsText())
        assertEquals(Status.NEW_GAME_STARTED.statusCode, response.status)
        assertEquals(Status.NEW_GAME_STARTED.message, ngr.status)
        assertEquals(1, ngr.gameId)
        assertEquals(example2Size, ngr.size)

        // 6. Request: POST /game/1/join
        // auth join by Artem - Success
        response = client.post("/game/1/join") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
        }
        val bodyDataMap = Json.decodeFromString<Map<String, String>>(response.bodyAsText())
        assertEquals(Status.JOINING_GAME_SUCCEEDED.statusCode, response.status)
        assertTrue(bodyDataMap.containsKey("status"))
        assertEquals(Status.JOINING_GAME_SUCCEEDED.message, bodyDataMap["status"])

        // 7. Request: POST /game/1/move
        // auth Artem (who will be playing as both Player1 and Player2) move (1,1) - Success
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(1,1)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        //val moveResponse: PlayerMoveResponsePayload = Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText())
        assertEquals(
            Status.MOVE_DONE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 8. Request: POST /game/1/move
        // auth Artem (who will be playing as both Player1 and Player2) move (1,2) - Success
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(1,2)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        assertEquals(
            Status.MOVE_DONE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 9. Request: POST /game/1/move
        // auth Artem (who will be playing as both Player1 and Player2) move (2,1) - Success
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(2,1)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        assertEquals(
            Status.MOVE_DONE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 10. Request: POST /game/1/move
        // auth Artem (who will be playing as both Player1 and Player2) move (2,2) - Success
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(2,2)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        assertEquals(
            Status.MOVE_DONE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 11. Request: POST /game/1/move
        // auth Artem (who will be playing as both Player1 and Player2) move (3,1) - Success
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(3,1)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        assertEquals(
            Status.MOVE_DONE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 12. Request: GET /game/1/status
        // auth status - Success
        response = client.get("/game/1/status") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
        }
        assertEquals(Status.GET_STATUS_SUCCEEDED.statusCode, response.status)
        assertEquals("1st player won", Json.decodeFromString<GameStatusResponsePayload>(response.bodyAsText()).status)

    }
}