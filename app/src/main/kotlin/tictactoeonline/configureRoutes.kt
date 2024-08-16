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
    SIGNED_IN_FAILED("Authorization failed", HttpStatusCode.Forbidden), // with JWT
    SIGNED_UP("Signed Up", HttpStatusCode.OK),
    AUTHORIZATION_FAILED("Authorization failed", HttpStatusCode.Unauthorized),
    INCORRECT_OR_IMPOSSIBLE_MOVE("Incorrect or impossible move", HttpStatusCode.BadRequest),
    NO_RIGHTS_TO_MOVE("You have no rights to make this move", HttpStatusCode.Forbidden),
    REGISTRATION_FAILED("Registration failed", HttpStatusCode.Forbidden),
    NEW_GAME_STARTED("New game started", HttpStatusCode.OK),
    CREATING_GAME_FAILED("Creating a game failed", HttpStatusCode.Forbidden),
    JOINING_GAME_SUCCEEDED("Joining the game succeeded", HttpStatusCode.OK),
    JOINING_GAME_FAILED("Joining the game failed", HttpStatusCode.Forbidden),
    GET_STATUS_FAILED("Failed to get game status", HttpStatusCode.Forbidden),
    GET_STATUS_SUCCEEDED(
        "Succeeded in getting game status - NOTE: this is not an official response",
        HttpStatusCode.OK
    ),
    MOVE_REQUEST_WITHOUT_AUTHORIZATION("Authorization failed", HttpStatusCode.Unauthorized),

    //    MOVE_REQUEST_WITHOUT_AUTHORIZATION("Authorization failed", HttpStatusCode.Forbidden),
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

/**
 * Game status only response payload
 * /games
 *
 * @property status
 * @constructor Create empty Game status only response payload
 */
@Serializable
data class GamesResponsePayload(
    @SerialName("game_id") val gameId: Int,
    @SerialName("player1") val playerXName: String? = null,
    @SerialName("player2") val playerOName: String? = null,
    @SerialName("size") val fieldDimensions: String? = null
)

@Serializable
data class GameStatusOnlyResponsePayload(@SerialName("game_status") val status: String)

@Serializable
data class GameStatusResponsePayload(
    @SerialName("game_id") val gameId: String,
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

@Serializable
data class Endpoint(
    val path: String,
    val method: String,
    val auth_required: Boolean,
    val description: String = "",
)

@Serializable
data class HelpPayload(val endpoints: List<Endpoint>)


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
                            gameId = (GameStore.indexOf(game) + 1).toString(),
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
                call.respond(
                    HttpStatusCode.Forbidden,
                    Json.encodeToString(mapOf("status" to Status.REGISTRATION_FAILED.message))
                )
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
            val ng = Json { ignoreUnknownKeys = true }.decodeFromString<PlayerSignupRequestPayload>(json)
            call.application.environment.log.info(ng.toString())
            val user = User(email = ng.email, password = ng.password)
            call.application.environment.log.info(
                """
                $json
            """.trimIndent()
            )
            // only signin users we know about and have a matching password
            if (UserStore.contains(user) && UserStore.find { storedUser -> user == storedUser }?.password == ng.password) {
                // good
                val secret = "ut920BwH09AOEDx5"
                val token = JWT.create()
                    .withClaim("email", user.email)
                    .sign(Algorithm.HMAC256(secret))
                user.jwt = token
                if (!UserSignedInStore.contains(user)) {
                    UserSignedInStore.add(user)
                }
                call.respond(mapOf("status" to Status.SIGNED_IN.message, "token" to token))
            } else {
                call.respond(
                    Status.SIGNED_IN_FAILED.statusCode,
//                        HttpStatusCode.Forbidden,
                    Json.encodeToString(mapOf("status" to Status.SIGNED_IN_FAILED.message))
                )

//                call.respond(
//                    HttpStatusCode.Unauthorized,
//                    Json.encodeToString(mapOf("status" to Status.SIGNED_UP.message))
//                )
            }
        }


        // JWT-protected routes
        authenticate("auth-jwt") {
            fun ApplicationCall.playerEmail(): String {
                return this.principal<JWTPrincipal>()!!.payload.getClaim("email").asString()
            }
            post("/game") {
                // ...
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                val principal = call.principal<JWTPrincipal>()
                val playerEmailAddress = call.playerEmail()
                val newGameRequestPayload = call.receive<NewGameRequestPayload>()

                require(UserStore.any { user -> user.email == playerEmailAddress })
                val user = UserStore.find { user -> user.email == playerEmailAddress }
                val newGame = TicTacToeOnline()
                newGame.initializeField(newGameRequestPayload.size)
                GameStore.add(newGame)
                val game_id = GameStore.indexOf(newGame) + 1
//                val ngrp = call.receive<NewGameRequestPayload>()
                if (newGameRequestPayload.isInvalid()) {
                    throw Exception(newGameRequestPayload.whyInvalid().toString())
                }
                var player1 = ""
                var player2 = ""
                if (newGameRequestPayload.player1.isNotEmpty()) {
                    newGame.playerX = Player(name = newGameRequestPayload.player1, marker = 'X')
                    player1 = newGameRequestPayload.player1
                    newGameRequestPayload.player1
                } else {
                    newGame.playerO = Player(name = newGameRequestPayload.player2, marker = 'O')
                    player2 = newGameRequestPayload.player2
                    newGameRequestPayload.player2
                }

                val respPayload = NewGameResponsePayload(
                    status = Status.NEW_GAME_STARTED.message,
                    player1 = player1,
                    player2 = player2,
                    gameId = game_id,
                    size = newGameRequestPayload.size
                )
                call.respond(respPayload)

            }

            post("/game/{game_id}/join") {
                call.parameters["game_id"]?.let { stringId ->
                    val playerEmail = call.playerEmail()
                    stringId.toIntOrNull()?.let { game_id ->
                        if (game_id in 0 until GameStore.lastIndex) {
                            call.respond(
                                Status.JOINING_GAME_FAILED.statusCode,
                                mapOf("status" to Status.JOINING_GAME_FAILED.message)
                            )
                        } else {
                            val game = GameStore[game_id - 1]
                            val ttt = game as TicTacToeOnline
                            // need utility to get player from JWT
                            val user = UserStore.find { user -> user.email == playerEmail }

                            ttt.addPlayer(Player(name = user?.email ?: "BOGUS-EMAIL", marker = 'X'))

                            @Serializable
                            data class StatusPayload(val status: String = Status.JOINING_GAME_SUCCEEDED.message)
                            call.respond(Status.JOINING_GAME_SUCCEEDED.statusCode, StatusPayload())
                        }
                    }
                }
            }

            post("/game/{game_id}/move") {
                call.parameters["game_id"]?.let { stringId ->
                    stringId.toIntOrNull()?.let { game_id ->
                        val playerEmail = call.playerEmail()
                        val game = GameStore[game_id - 1]
                        val ttt = game as TicTacToeOnline
                        val playerMoveRequestPayload = call.receive<PlayerMoveRequestPayload>()
                        val move = playerMoveRequestPayload.move

                        if (ttt.currentPlayer.name != playerEmail) {
                            // if it's not your turn, then you have no right
                            call.respond(
                                Status.NO_RIGHTS_TO_MOVE.statusCode,
                                PlayerMoveResponsePayload(Status.NO_RIGHTS_TO_MOVE.message)
                            )
                        } else if (ttt.isValidMove(move) && ttt.isOccupied(move)) {
                            // fail, move
                            call.respond(
                                Status.INCORRECT_OR_IMPOSSIBLE_MOVE.statusCode,
                                PlayerMoveResponsePayload(Status.INCORRECT_OR_IMPOSSIBLE_MOVE.message)
                            )
                        } else if (ttt.isValidMove(move) && ttt.move(move)) {
                            // success
                            call.respond(
                                Status.MOVE_DONE.statusCode,
                                PlayerMoveResponsePayload(Status.MOVE_DONE.message)
                            )
                        } else {
                            call.respond(
                                Status.NO_RIGHTS_TO_MOVE.statusCode,
                                PlayerMoveResponsePayload(Status.NO_RIGHTS_TO_MOVE.message)
                            )
                        }
                    }
                }
            }

            get("/game/{game_id}/status") {
                call.parameters["game_id"]?.let { stringId ->
                    stringId.toIntOrNull()?.let { game_id ->
                        val game = GameStore[game_id - 1]
                        val ttt = game as TicTacToeOnline
                        val gsrp = GameStatusResponsePayload(
                            gameId = (GameStore.indexOf(game) + 1).toString(),
                            status = ttt.state.description,
                            field2DArray = ttt.renderFieldTo2DArray(),
                            playerXName = ttt.playerX.name,
                            playerOName = ttt.playerO.name,
                            fieldDimensions = ttt.fieldSize(),
                        )
                        call.respond(Status.GET_STATUS_SUCCEEDED.statusCode, gsrp)
//                        games[game_id]?.let { user ->
//                            call.respondText(user)
//                        }
                    }
                }
            }

            get("/games") {
                // ...
                val gamesResponses = mutableListOf<GamesResponsePayload>()
                GameStore.mapIndexed { idx, game ->
                    if (game is TicTacToeOnline) {
                        gamesResponses.add(
                            with(game) {
                                GamesResponsePayload(
                                    gameId = idx + 1,
                                    playerXName = game.playerX.name,
                                    playerOName = game.playerO.name,
                                    fieldDimensions = game.fieldSize()
                                )
                            })
                    } else {
                        throw Exception("Unknown game type, only know about TicTacToeOnline")
                    }
                }
                call.respond(gamesResponses.toList())
            }

        }

    }
}

fun clearAll() {
    GameStore.clear()
    UserSignedInStore.clear()
    UserStore.clear()
}


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
            Endpoint(
                "/game/1/join",
                method = HttpMethod.Post.value,
                description = "join a pre-existing game",
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


