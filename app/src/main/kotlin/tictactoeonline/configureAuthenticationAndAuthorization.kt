package tictactoeonline

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Application.configureAuthenticationAndAuthorization() {
    // Configuring JWT authorization
    val secret = "ut920BwH09AOEDx5"
    val myRealm = "Access to game"
    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null

                }
//                if (credential.payload.getClaim("email") !is NullClaim)
//                    JWTPrincipal(credential.payload)
//                else
//                    null
            }
            challenge { defaultScheme, realm ->
                if (call.request.uri.lowercase().contains("move")) {
                    println("WE ARE HERE ${call.request.uri}")
                }
                val status: Status =
                    if (call.request.uri.lowercase().contains("move")) Status.MOVE_REQUEST_WITHOUT_AUTHORIZATION
                    else Status.AUTHORIZATION_FAILED
                call.respond(status.statusCode, mapOf("status" to status.message))
//                call.respond(
//                    Status.AUTHORIZATION_FAILED.statusCode,
//                    mapOf("status" to Status.AUTHORIZATION_FAILED.message)
//                )
            }
        }
    }

    // Configuring the authorization error message
//    install(StatusPages) {call, status ->
//        status(HttpStatusCode.Unauthorized) {
//
//        }
////        status(HttpStatusCode.Unauthorized) {
////            call.response.status(HttpStatusCode.Unauthorized)
////            call.respond(
////                mapOf(
////                    "status" to "Authorization failed"
////                )
////            )
////        }
//    }

}