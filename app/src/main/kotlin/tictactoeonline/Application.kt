package tictactoeonline

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
//import io.ktor.serialization.kotlinx.json.*
//import io.ktor.server.application.*
//import io.ktor.server.netty.*
//import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ThePayload(val player1: String = "Carl", val player2: String = "Mike", val size: String = "4x3")

@Serializable
data class NewGameRequestPayload(val player1: String, val player2: String, val size: String)

@Serializable
data class NewGameResponsePayload(
    @SerialName("status") val status: String, val player1: String, val player2: String, val size: String
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

class MyApplication() {
    fun start(args: Array<String>) {
        EngineMain.main(args)
    }
}

var game: TicTacToeOnline = TicTacToeOnline()

fun main(args: Array<String>) {
    println(Json { prettyPrint = true }.encodeToString("jed" to "wilma"))
//    io.ktor.server.netty.EngineMain.main(args)
//    EngineMain.main(args)
    MyApplication().start(args)
}


fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Optional configuration
            encodeDefaults = true
        })
    }

    fun showQueryParameters(call: ApplicationCall) {
        call.application.environment.log.info("No. parameters: ${call.request.queryParameters.entries().size}")
        call.request.queryParameters.forEach { key, value ->
            call.application.environment.log.info("$key : $value")
        }
    }

    routing {
        // put your routes here
        route("/game") {
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
                            player1 = "Player1", player2 = "Player2", size = "3x3", status = "New game started"
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
                            status = "New game started"
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
}