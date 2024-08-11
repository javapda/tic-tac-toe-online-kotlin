/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.8/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
//    alias(libs.plugins.jvm)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
//    alias(libs.plugins.kotlin.plugin.serialization)
    kotlin("plugin.serialization").version("1.9.23")
//    id("io.ktor.plugin").version("2.3.12")
    // Apply the application plugin to add support for building a CLI application in Java.
    application

}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}
val ktor_version = "2.3.12"
val logback_version = "1.4.14"
val kotlin_version = "1.9.23"

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)


    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.status.pages)

//    implementation(lbibs.kotlin.serialization.json)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.logback)
    testImplementation(libs.ktor.server.test.host)
//    testImplementation(libs.kotlin.test.junit)  // removed due to conflict during testing
//    implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktor_version")



}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
//    mainClass = "org.example.AppKt"
    mainClass.set("io.ktor.server.netty.EngineMain")

}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}