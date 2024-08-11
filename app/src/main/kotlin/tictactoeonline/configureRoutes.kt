package tictactoeonline

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class Status(val message: String, val statusCode: HttpStatusCode) {
    SIGNED_IN("Signed In", HttpStatusCode.OK), // with JWT
    SIGNED_UP("Signed Up", HttpStatusCode.OK),
    AUTHORIZATION_FAILED("Authorization failed", HttpStatusCode.Unauthorized),
    INCORRECT_OR_IMPOSSIBLE_MOVE("Incorrect or impossible move", HttpStatusCode.OK),
    NO_RIGHTS_TO_MOVE("You have no rights to make this move", HttpStatusCode.Forbidden),
    REGISTRATION_FAILED("Registration failed", HttpStatusCode.Forbidden),
    NEW_GAME_STARTED("New game started", HttpStatusCode.OK),
    CREATING_GAME_FAILED("Creating a game failed", HttpStatusCode.Forbidden),
    JOINING_GAME_SUCCEEDED("Joining the game succeeded", HttpStatusCode.OK),
    JOINING_GAME_FAILED("Joining the game failed", HttpStatusCode.Forbidden),
    GET_STATUS_FAILED("Failed to get game status", HttpStatusCode.Forbidden),
    MOVE_DONE("Move done", HttpStatusCode.OK),
}

@Serializable
data class User(val email: String, val password: String) {
    var jwt: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return email == other.email
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }
}

@Serializable
data class PlayerSignupRequestPayload(val email: String, val password: String)

@Serializable
data class PlayerSigninResponsePayload(val status: String, val token: String)

@Serializable
data class NewGameRequestPayload(val player1: String, val player2: String, val size: String) {
    enum class NewGameRequestPayloadError(description: String) {
        BOTH_PLAYER_MISSING_EMAIL_ADDRESS("both player missing email address"),
        BOTH_PLAYER_EMAIL_ADDRESSES_PRESENT("both player email address present, should only have one"),
        INVALID_FIELD_DIMENSIONS_PROVIDED("invalid field dimensions provided")
    }

    fun isValid(): Boolean {
        return ((player1.isNotEmpty() && player2.isEmpty()) ||
                (player1.isEmpty() && player2.isNotEmpty()))
                && PlayingGrid.isValidFieldDimensionString(size)
    }

    fun isInvalid() = !isValid()
    fun whyInvalid(): Set<NewGameRequestPayloadError> {
        if (isValid()) {
            throw Exception("I am valid: $this")
        }
        val errors = mutableSetOf<NewGameRequestPayloadError>()
        if (player1.isEmpty() && player2.isEmpty()) errors.add(NewGameRequestPayloadError.BOTH_PLAYER_MISSING_EMAIL_ADDRESS)
        if (player1.isNotEmpty() && player2.isNotEmpty()) errors.add(NewGameRequestPayloadError.BOTH_PLAYER_EMAIL_ADDRESSES_PRESENT)
        if (!PlayingGrid.isValidFieldDimensionString(size)) errors.add(NewGameRequestPayloadError.INVALID_FIELD_DIMENSIONS_PROVIDED)
        return errors.toSet()
    }

}

@Serializable
data class NewGameResponsePayload(
    @SerialName("status") val status: String,
    val player1: String,
    val player2: String,
    val size: String,
    @SerialName("game_id") val gameId: Int
)


@Serializable
data class PlayerMoveRequestPayload(val move: String)


@Serializable
data class PlayerMoveResponsePayload(@SerialName("status") val status: String)

@Serializable
data class GameStatusOnlyResponsePayload(@SerialName("game_status") val status: String)

@Serializable
data class GameStatusResponsePayload(
    @SerialName("game_status") val status: String,
    @SerialName("field") val field2DArray: List<List<String>>? = null,
    @SerialName("player1") val playerXName: String? = null,
    @SerialName("player2") val playerOName: String? = null,
    @SerialName("size") val fieldDimensions: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameStatusResponsePayload

        if (status != other.status) return false
        if (field2DArray != other.field2DArray) return false
        if (playerXName != other.playerXName) return false
        if (playerOName != other.playerOName) return false
        if (fieldDimensions != other.fieldDimensions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + (field2DArray?.hashCode() ?: 0)
        result = 31 * result + (playerXName?.hashCode() ?: 0)
        result = 31 * result + (playerOName?.hashCode() ?: 0)
        result = 31 * result + (fieldDimensions?.hashCode() ?: 0)
        return result
    }
}

val UserStore: MutableList<User> = mutableListOf()
val UserSignedInStore: MutableList<User> = mutableListOf()
val GameStore: MutableList<Game> = mutableListOf()

fun Application.configureRouting() {

    // Game and user storage (the Game and User classes you should implement yourself)
//    val GameStore: MutableList<Game> = mutableListOf()
//    val UserStore: MutableList<User> = mutableListOf()
    routing {
        // put your routes here
        route("/gameOLD") {
            post("") {
                var responded = false
                // New game requested
                // If the user sent invalid data or did not send the appropriate fields at all,
                // then set the default values
                val possibleJsonText = call.receiveText()
                var ng: NewGameRequestPayload? = null
                call.application.environment.log.info("-->\n$possibleJsonText\n<--")
                try {
                    ng = Json.decodeFromString<NewGameRequestPayload>(possibleJsonText)

                } catch (e: Exception) {
                    call.application.environment.log.info("Exception: ${e.message}")
                    call.application.environment.log.info("Setting Default Values")
                    if (game.newGame("Player1", "Player2", "3x3")) {
                        val response = NewGameResponsePayload(
                            player1 = "Player1",
                            player2 = "Player2",
                            size = "3x3",
                            status = "New game started",
                            gameId = -23
                        )
                        call.respond(response)
                        responded = true
                    } else {
                        throw IllegalStateException("M1:Expected game to be started, but it is not")
                    }

                }
                if (!responded && !PlayingGrid.isValidFieldDimensionString(ng!!.size)) {
                    ng = ng.copy(size = "3x3")
                }

                if (!responded && ng != null && game.newGame(ng.player1, ng.player2, ng.size)) {
                    if (game.gameStarted()) {
                        val response = NewGameResponsePayload(
                            player1 = game.playerX.name,
                            player2 = game.playerO.name,
                            size = game.fieldSize(),
                            status = "New game started",
                            gameId = -23
                        )
                        call.respond(response)
                    } else {
                        throw IllegalStateException("M2:Expected game to be started, but it is not")
                    }

                } else {
                    if (!responded) {
                        call.respond(mapOf("Error" to "Something went wrong on ${call.request.path()}"))
                    }
                }

            }
            post("move") {
                val ng = call.receive<PlayerMoveRequestPayload>()
                call.application.environment.log.info(ng.toString())
                if (game.isValidMove(ng.move)) {
                    if (game.move(ng.move)) {
                        call.respond(PlayerMoveResponsePayload("Move done"))
                    } else {

                        call.respond(
                            HttpStatusCode.BadRequest, PlayerMoveResponsePayload("Incorrect or impossible move")
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, PlayerMoveResponsePayload("Incorrect or impossible move"))

                }
            }
            get("status") {
                call.respond(
                    if (game.state != GameState.NOT_STARTED) {
                        GameStatusResponsePayload(
                            status = game.state.description,
                            field2DArray = game.renderFieldTo2DArray(),
                            playerXName = game.playerX.name,
                            playerOName = game.playerO.name,
                            fieldDimensions = game.fieldSize(),
                        )
                    } else {
                        GameStatusOnlyResponsePayload(status = game.state.description)
                    }
                )
            }
        }
    }

    routing {

        get("/helloWorld") {
            call.respondText("Hello World!")
        }
        get("/help") {
            call.respond(help())
        }

        get("/info") {
            call.respond(Json.encodeToString(info(call)))
        }

        delete("/clearAll") {
            val clearInfo = buildString {
                appendLine("Deleted via ${call.request.uri}")
            }
            clearAll()
            call.respondText(clearInfo)
        }


        // Routes not protected by the JWT
        post("/signup") {
            // ...
            val json = call.receiveText()
            call.application.environment.log.info(
                """
                $json
            """.trimIndent()
            )
            var ng: PlayerSignupRequestPayload? = null
            try {
                ng = Json.decodeFromString<PlayerSignupRequestPayload>(json)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Forbidden, Status.REGISTRATION_FAILED.message)
                return@post
            }
            call.application.environment.log.info(ng.toString())
            val user = User(email = ng!!.email, password = ng!!.password)
            if (user.email.isEmpty() || user.password.isEmpty() || UserStore.contains(user)) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Json.encodeToString(mapOf("status" to Status.REGISTRATION_FAILED.message))
                )
            } else {
                UserStore.add(user)
                call.respondText(Json.encodeToString(mapOf("status" to "Signed Up")))
            }
        }

        post("/signin") {
            val json = call.receiveText()
            val ng = Json.decodeFromString<PlayerSignupRequestPayload>(json)
            call.application.environment.log.info(ng.toString())
            val user = User(email = ng.email, password = ng.password)
            call.application.environment.log.info(
                """
                $json
            """.trimIndent()
            )
            if (UserStore.contains(user)) {
                // good
                val secret = "ut920BwH09AOEDx5"
//                val audience = "myAudienceHere"
//                val issuer = "Mr-Issuer"
                val token = JWT.create()
//                    .withAudience(audience)
//                    .withIssuer(issuer)
//                    .withClaim("email", user.email)
                    .withPayload(mapOf("email" to user.email))
// Payload usually contains token expiration time in real projects, but we will not include it for simplicity.
//                    .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60000))
                    .sign(Algorithm.HMAC256(secret))
                user.jwt = token
                if (!UserSignedInStore.contains(user)) {
                    UserSignedInStore.add(user)
                }
                call.respond(mapOf("status" to Status.SIGNED_IN.message, "token" to token))
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    Json.encodeToString(mapOf("status" to Status.SIGNED_UP.message))
                )
            }
        }


        // JWT-protected routes
        authenticate("auth-jwt") {

            post("/game") {
                // ...
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                val principal = call.principal<JWTPrincipal>()
                val playerEmailAddress = principal!!.payload.getClaim("email").asString()

//                val username = principal!!.payload.getClaim("username").asString()
//                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                println(
                    """
                    ${"*".repeat(80)}
                    Auth Header Time
                    authHeader:  $authHeader
                    playerEmailAddress:  $playerEmailAddress
                    ${"*".repeat(80)}
                """.trimIndent()
                )
                require(UserStore.any { user -> user.email == playerEmailAddress })
                val user = UserStore.find { user -> user.email == playerEmailAddress }
                val newGame = TicTacToeOnline()
                GameStore.add(newGame)
                val game_id = GameStore.indexOf(newGame) + 1
                val ngrp = call.receive<NewGameRequestPayload>()
                if (ngrp.isInvalid()) {
                    throw Exception(ngrp.whyInvalid().toString())
                }
                var player1=""
                var player2=""
                    if (ngrp.player1.isNotEmpty()) {
                        newGame.playerX = Player(name = ngrp.player1, marker = 'X')
                        player1=ngrp.player1
                        ngrp.player1
                    } else {
                        newGame.playerO = Player(name = ngrp.player1, marker = 'O')
                        player2=ngrp.player2
                        ngrp.player2
                    }

                val respPayload = NewGameResponsePayload(status=Status.NEW_GAME_STARTED.message,player1=player1,player2=player2, gameId = game_id,size=ngrp.size)
                call.respond(respPayload)

            }

            post("/game/{game_id}/move") {
                call.parameters["game_id"]?.let { stringId ->
                    stringId.toIntOrNull()?.let { game_id ->
                        call.respondText("TODO: do move for game_id=$game_id")
//                        games[game_id]?.let { user ->
//                            call.respondText(user)
//                        }
                    }
                }
            }

            get("/game/{game_id}/status") {
                call.parameters["game_id"]?.let { stringId ->
                    stringId.toIntOrNull()?.let { game_id ->
                        call.respondText("TODO: do status for game_id=$game_id")
//                        games[game_id]?.let { user ->
//                            call.respondText(user)
//                        }
                    }
                }
            }

            get("/games") {
                // ...
                call.respondText("TODO: GET response to ${call.request.uri}")
            }

            // ...

        }

    }
}

fun clearAll() {
    GameStore.clear()
    UserSignedInStore.clear()
    UserStore.clear()
}

@Serializable
data class InfoPayload(
    val num_users: Int = UserStore.size,
    val uri: String,
    val port: Int,
    val http_method: String,
    val num_parameters: Int,
    val users: MutableList<User>,
    val num_users_signin: Int = UserSignedInStore.size,
    val users_signin: MutableList<User>,
)

fun info(call: ApplicationCall? = null): InfoPayload {

    return InfoPayload(
        num_users = UserStore.size,
        users = UserStore,
        uri = call?.request?.uri ?: "no-call",
        port = call?.request?.port() ?: -1,
        http_method = call?.request?.httpMethod?.toString() ?: "no-call",
        num_parameters = call?.parameters?.entries()?.size ?: -1,
        num_users_signin = UserSignedInStore.size,
        users_signin = UserSignedInStore
    )
}

@Serializable
data class Endpoint(
    val path: String,
    val method: String,
    val auth_required: Boolean,
    val description: String = "",
)

@Serializable
data class HelpPayload(val endpoints: List<Endpoint>)

fun help(): HelpPayload =
    HelpPayload(
        endpoints = listOf(
            Endpoint("/info", method = HttpMethod.Get.value, description = "show information", auth_required = false),
            Endpoint("/help", method = HttpMethod.Get.value, description = "show this help", auth_required = false),
            Endpoint(
                "/clearAll",
                method = HttpMethod.Delete.value,
                description = "clear all user, signin, and game data", auth_required = false
            ),
            Endpoint(
                "/signup",
                method = HttpMethod.Post.value,
                description = "register an email address with the website", auth_required = false
            ),
            Endpoint(
                "/signin",
                method = HttpMethod.Post.value,
                description = "let the system know you are online",
                auth_required = false
            ),
            Endpoint(
                "/helloWorld",
                method = HttpMethod.Get.value,
                description = "return Hello, World!",
                auth_required = false
            ),
            Endpoint(
                "/game",
                method = HttpMethod.Post.value,
                description = "used to request a new game",
                auth_required = true
            ),
            Endpoint(
                "/games",
                method = HttpMethod.Get.value,
                description = "getting a list of all games (game rooms)",
                auth_required = true
            ),
            Endpoint("/game/1/move", method = HttpMethod.Post.value, description = "", auth_required = true),
            Endpoint(
                "/game/1/status",
                method = HttpMethod.Get.value,
                description = "show status of a game with game_id=1",
                auth_required = true
            ),
        )
    )


