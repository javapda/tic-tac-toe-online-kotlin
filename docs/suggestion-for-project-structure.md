## Our suggestion for the structure of your project is as follows:
[ktor](https://ktor.io) | [application.conf : HOCON](https://ktor.io/docs/server-configuration-file.html)
![This is an alt text.](images/suggestion-for-project-structure.webp "Suggestion fo project structure.")
```
// application.conf

ktor {
    deployment {
        port = 28852
    }
    application {
        modules = [tictactoeonline.ApplicationKt.module]
    }
}
```
```
// Application.kt
package tictactoeonline

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


fun Application.module(testing: Boolean = false) {

    // Install ContentNegotiation plugin for JSON serialization and deserialization
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Optional configuration
            encodeDefaults = true
        })
    }

    routing {
        // put your routes here
        post("/game") {
            // ...
        }
        get("/game/status") {
            // ...
        }
        // ...
    }
}
```

```
// build.gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-serialization:$hs.kotlin.version"
    }
}

apply plugin: 'java'
apply plugin: 'org.jetbrains.kotlin.jvm'
apply plugin: 'kotlinx-serialization'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}
sourceSets.main.resources.srcDirs = ["src/resources"]

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib"
    implementation "io.ktor:ktor-server-core:1.6.7"
    implementation "io.ktor:ktor-server-netty:1.6.7"
    implementation "ch.qos.logback:logback-classic:1.2.10"
    implementation "io.ktor:ktor-serialization:1.6.7"
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0"
    testImplementation "io.ktor:ktor-server-test-host:1.6.7"
}
```