package tictactoeonline

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureContentNegotiationForJsonSerializationAndDeserialization() {
    // Install ContentNegotiation plugin for JSON serialization and deserialization
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Optional configuration
            encodeDefaults = true
        })
    }

}
