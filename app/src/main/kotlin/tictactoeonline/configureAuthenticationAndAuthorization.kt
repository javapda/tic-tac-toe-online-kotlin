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
            }
            challenge { defaultScheme, realm ->
                val status: Status =
                    if (call.request.uri.lowercase().contains("move")) Status.MOVE_REQUEST_WITHOUT_AUTHORIZATION
                    else Status.AUTHORIZATION_FAILED
                call.respond(status.statusCode, mapOf("status" to status.message))
            }
        }
    }


}