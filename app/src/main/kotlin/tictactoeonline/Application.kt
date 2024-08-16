package tictactoeonline

//import io.ktor.serialization.kotlinx.json.*
//import io.ktor.server.application.*
//import io.ktor.server.netty.*
//import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class MyApplication() {
    fun start(args: Array<String>) {
        EngineMain.main(args)
    }
}

var game: TicTacToeOnline = TicTacToeOnline()

private val json = Json { prettyPrint = true }

fun main(args: Array<String>) {
//    println(json.encodeToString("jed" to "wilma"))
//    io.ktor.server.netty.EngineMain.main(args)
//    EngineMain.main(args)
    MyApplication().start(args)
}


/**
 * Application Testing
 * Used to prevent maintaining data between runs of the test infrastructure
 */
var APPLICATION_TESTING = false
fun Application.module(testing: Boolean = false) {
    if (!APPLICATION_TESTING) clearAll()
    fun showQueryParameters(call: ApplicationCall) {
        call.application.environment.log.info("No. parameters: ${call.request.queryParameters.entries().size}")
        call.request.queryParameters.forEach { key, value ->
            call.application.environment.log.info("$key : $value")
        }
    }
    configureAuthenticationAndAuthorization()
    configureRouting()
    configureContentNegotiationForJsonSerializationAndDeserialization()
}