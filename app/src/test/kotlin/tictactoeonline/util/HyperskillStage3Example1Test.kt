package tictactoeonline.util

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
import tictactoeonline.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HyperskillStage3Example1Test {
    @BeforeEach
    fun setup() {
        clearAll()
    }


    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `Example 1 signup and signin two people`() = testApplication {
        lateinit var user1: User
        lateinit var user2: User
        lateinit var response: HttpResponse
        // add a person
        var email1 = "carl@example.com"
        var password1 = "1111"
        var email2 = "mike@example.com"
        var password2 = "2222"
        val example1Size = "4x3"

        // 1. Request: POST /game
        // fail /game first - no auth - failure
        response = client.post("/game") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            user1 = User(email = email1, password = password1)
            val json = Json.encodeToString(user1)
            setBody(json)
        }
        val ma: Map<String, String> = Json.decodeFromString<Map<String, String>>(response.bodyAsText())
        assertEquals(Status.AUTHORIZATION_FAILED.statusCode, response.status)
        assertEquals(Status.AUTHORIZATION_FAILED.message, ma["status"])

        // 2. Request: POST /signup
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

        // 3. Request: POST /signup
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

        // 4. Request: POST /signin
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
        assertEquals(1, UserSignedInStore.size)
        assertEquals(1, info().num_users_signin)

        // 5. Request: POST /signin
        // signin Player 2
        response = client.post("/signin") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val json = Json.encodeToString(user2)
            setBody(json)
        }
        playerSigninResponsePayload = Json.decodeFromString(response.bodyAsText())
        user2.jwt = playerSigninResponsePayload.token
        assertEquals(Status.SIGNED_IN.message, playerSigninResponsePayload.status)
        assertEquals(2, UserSignedInStore.size)
        assertEquals(2, info().num_users_signin)

        // 6. Request: POST /game
        // successfully /game first
        response = client.post("/game") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            val payloadOfJwt: String = JWT().decodeJwt(user1.jwt).payload
            val contents = String(Base64.decode(payloadOfJwt))

            @Serializable
            data class Payload(val email: String)

            val payload = Json.decodeFromString<Payload>(contents)
            val email = payload.email
            header("Authorization", "Bearer ${user1.jwt}")

            val ngr: NewGameRequestPayload =
                NewGameRequestPayload(player1 = user1.email, player2 = "", size = example1Size)
            val json = Json.encodeToString(ngr)
            setBody(json)
        }
        val bodyJsonHere = response.bodyAsText()
        val ngr: NewGameResponsePayload = Json.decodeFromString<NewGameResponsePayload>(bodyJsonHere)
        assertEquals(Status.NEW_GAME_STARTED.statusCode, response.status)
        assertEquals(Status.NEW_GAME_STARTED.message, ngr.status)
        assertEquals(1, ngr.gameId)
        assertEquals(example1Size, ngr.size)

        // 7. Request: POST /game/1/join
        // auth join by Player2 (user2) - Success
        response = client.post("/game/1/join") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user2.jwt}")
        }
        val newGameResponseBodyJsonHere = response.bodyAsText()
        val bodyDataMap: Map<String, String> = Json.decodeFromString(response.bodyAsText())
        assertEquals(Status.JOINING_GAME_SUCCEEDED.statusCode, response.status)
        assertTrue(bodyDataMap.containsKey("status"))
        assertEquals(Status.JOINING_GAME_SUCCEEDED.message, bodyDataMap["status"])

        // 8. Request: GET /game/1/status
        response = client.get("/game/1/status") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
        }
        assertEquals(Status.GET_STATUS_SUCCEEDED.statusCode, response.status)
        val gameStatusResponsePayload: GameStatusResponsePayload = Json.decodeFromString(response.bodyAsText())
        assertEquals("1st player's move", gameStatusResponsePayload.status)

        // 9. Request: POST /game/1/move
        // 1st move by Carl Player1 - successful move to (1,1)
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(1,1)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        val moveResponse: PlayerMoveResponsePayload = Json.decodeFromString(response.bodyAsText())
        assertEquals(Status.MOVE_DONE.message, moveResponse.status)

        // 10. Request: POST /game/1/move
        // move request without authorization header, failure, 401 Unauthorized
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
//            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(1,1)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_REQUEST_WITHOUT_AUTHORIZATION.statusCode, response.status)
        val moveResponseWithoutAuth: PlayerMoveResponsePayload = Json.decodeFromString(response.bodyAsText())
        assertEquals(Status.MOVE_REQUEST_WITHOUT_AUTHORIZATION.message, moveResponseWithoutAuth.status)

        // 11. Request: POST /game/1/move
        // at this point a move to (1,1) has already been done
        // here, we send the JWT for user : carl@example.com, but it is the same move and space already taken
        // or maybe, it is not Carl's turn (he did the last move) - it should be Mike's (Player2's) turn
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(1,1)"))
            setBody(json)
        }
        assertEquals(Status.NO_RIGHTS_TO_MOVE.statusCode, response.status)
        val moveResponseWithoutAuthAgain: PlayerMoveResponsePayload = Json.decodeFromString(response.bodyAsText())
        assertEquals(Status.NO_RIGHTS_TO_MOVE.message, moveResponseWithoutAuthAgain.status)

        // 12. Request: POST /game/1/move
        // authorized request move by mike (Player2) to an occupied place (1,1)
        // result will be a failure, 400 Bad Request,  "status": "Incorrect or impossible move"
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user2.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(1,1)"))
            setBody(json)
        }
        assertEquals(Status.INCORRECT_OR_IMPOSSIBLE_MOVE.statusCode, response.status)
        assertEquals(
            Status.INCORRECT_OR_IMPOSSIBLE_MOVE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 13. Request: POST /game/1/move
        // auth move by mike (Player2) to an available position (2,1) - Success
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user2.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(2,1)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        assertEquals(
            Status.MOVE_DONE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 14. Request: POST /game/1/move
        // auth move by Carl (Player1) to (1,2) - Success
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

        // 15. Request: POST /game/1/move
        // auth move by Mike (Player2) to (2,2)
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user2.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(2,2)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        assertEquals(
            Status.MOVE_DONE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 16. Request: POST /game/1/move
        // auth move by Carl (Player1) to (1,3)
        response = client.post("/game/1/move") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
            val json = Json.encodeToString(PlayerMoveRequestPayload("(1,3)"))
            setBody(json)
        }
        assertEquals(Status.MOVE_DONE.statusCode, response.status)
        assertEquals(
            Status.MOVE_DONE.message,
            Json.decodeFromString<PlayerMoveResponsePayload>(response.bodyAsText()).status
        )

        // 17. Request: GET /game/1/status
        // auth request by Carl (Player1) for status - Success
        response = client.get("/game/1/status") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${user1.jwt}")
        }
        assertEquals(Status.GET_STATUS_SUCCEEDED.statusCode, response.status)
        val gameStatusResponsePayloadAfterStep16 =
            Json.decodeFromString<GameStatusResponsePayload>(response.bodyAsText())
        assertEquals("1st player won", gameStatusResponsePayloadAfterStep16.status)

    }

}