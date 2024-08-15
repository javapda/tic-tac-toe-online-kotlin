package tictactoeonline

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import java.security.InvalidParameterException

fun Application.configureStatusPages() {
    install(StatusPages) {
//        status(HttpStatusCode.Unauthorized) { call, status ->
//            call.response.status(HttpStatusCode.Unauthorized)
//            call.respond(
//                mapOf(
//                    "status" to "Authorization failed"
//                )
//            )
//
//        }
//        status(HttpStatusCode.BadRequest) { call, status ->
//            call.respondText(text = "${status.value}: ${status.description} :(", status = HttpStatusCode.BadRequest)
//        }
//        status(HttpStatusCode.NotFound) { call, status ->
//            call.respondText(text = "${status.value}: ${status.description} :(", status = HttpStatusCode.NotFound)
//        }
//        status(HttpStatusCode.InternalServerError) { call, status ->
//            call.respondText(
//                text = "${status.value}: ${status.description} :(",
//                status = HttpStatusCode.InternalServerError
//            )
//        }
//
//        exception<InvalidParameterException> { call, cause ->
//            call.respondText(text = "400: Parameter is invalid", status = HttpStatusCode.BadRequest)
//        }

    }

}

fun Xmain() {
    println(
        """
        HttpStatusCode.InternalServerError:              ${HttpStatusCode.InternalServerError}
        HttpStatusCode.InternalServerError.description:  ${HttpStatusCode.InternalServerError.description}
        HttpStatusCode.InternalServerError.value:        ${HttpStatusCode.InternalServerError.value}
        ${"-".repeat(80)}
        HttpStatusCode.NotFound:              ${HttpStatusCode.NotFound}
        HttpStatusCode.NotFound.description:  ${HttpStatusCode.NotFound.description}
        HttpStatusCode.NotFound.value:        ${HttpStatusCode.NotFound.value}
        ${"-".repeat(80)}
        HttpStatusCode.BadRequest:              ${HttpStatusCode.BadRequest}
        HttpStatusCode.BadRequest.description:  ${HttpStatusCode.BadRequest.description}
        HttpStatusCode.BadRequest.value:        ${HttpStatusCode.BadRequest.value}
    """.trimIndent()
    )
}