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

class GamesTest {
    @BeforeEach
    fun setup() {
        clearAll()
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `test games need at least 2 games`() {
        APPLICATION_TESTING=true
        // start a game
        fun doGame(emailUser1: String, emailUser2: String, fieldSize: String = "3x3", gameId: Int) {
            testApplication {
                // signup players
                lateinit var user1: User
                lateinit var user2: User
                lateinit var response: HttpResponse
                // add a person
                var email1 = emailUser1
                var password1 = "1111"
                var email2 = emailUser2
                var password2 = "2222"
                val example1Size = fieldSize

                response = client.post("/signup") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    user1 = User(email = email1, password = password1)
                    val json = Json.encodeToString(user1)
                    setBody(json)
                }
                assertEquals(1 + ((gameId - 1) * 2), UserStore.size)
                response = client.post("/signup") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    user2 = User(email = email2, password = password1)
                    val json = Json.encodeToString(user2)
                    setBody(json)
                }

                // sign in players
                response = client.post("/signin") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(Json.encodeToString(user1))
                }
                user1 = UserSignedInStore.find { user -> user == user1 }!!
                response = client.post("/signin") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(Json.encodeToString(user2))
                }
                user2 = UserSignedInStore.find { user -> user == user2 }!!

                // start a game
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
                assertEquals(gameId, GameStore.size)

                // join the game
                response = client.post("/game/$gameId/join") {
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer ${user2.jwt}")
                }


            }
        }

        fun gamesCheck(user: User, expectedGameCount: Int) {
            testApplication {
                // games call
                val response = client.get("/games") {
                    header(HttpHeaders.Authorization, "Bearer ${user.jwt}")
                }
                assertEquals(HttpStatusCode.OK, response.status)
                val payload = Json.decodeFromString<List<GamesResponsePayload>>(response.bodyAsText())
                assertEquals(expectedGameCount, payload.size)
            }
        }

        fun gamesCheckWithoutAuthorization(user: User) {
            testApplication {
                // games call - without authorization
                val response = client.get("/games")
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }
        }


        doGame("carl@example.com", "mike@example.com", "4x3", 1)
        gamesCheck(UserSignedInStore.first(), 1)
        gamesCheckWithoutAuthorization(UserSignedInStore.first())
        doGame("artem@example.com", "artem@example.com", "3x3", 2)
        gamesCheck(UserSignedInStore.first(), 2)
    }


}