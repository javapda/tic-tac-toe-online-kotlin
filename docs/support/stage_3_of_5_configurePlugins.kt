package tictactoeonline

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.impl.NullClaim
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json


fun Application.configurePlugins() {

    // Install ContentNegotiation plugin for JSON serialization and deserialization
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Optional configuration
            encodeDefaults = true
        })
    }

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
                if (credential.payload.getClaim("email") !is NullClaim)
                    JWTPrincipal(credential.payload)
                else
                    null
            }
        }
    }

    // Configuring the authorization error message
    install(StatusPages) {
        status(HttpStatusCode.Unauthorized) {
            call.response.status(HttpStatusCode.Unauthorized)
            call.respond(
                mapOf(
                    "status" to "Authorization failed"
                )
            )
        }
    }

}