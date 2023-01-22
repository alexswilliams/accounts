import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION") // suppress intellij 2022.3 bug
plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktor)
}

group = "io.github.alexswilliams"
version = "1.0.0"

application {
    mainClass.set("io.github.alexswilliams.ServerMainKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.host.common.jvm)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.serialization.jackson.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.jackson)
    implementation(libs.jackson.jsr310)

    runtimeOnly(libs.logback.classic)

    testImplementation(libs.ktor.server.tests.jvm)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
}

val jvmVersion: String by project
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = jvmVersion
    }
}
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmVersion))
    }
}

sourceSets {
    main {
        kotlin.setSrcDirs(setOf("src/main/kotlin"))
        resources.setSrcDirs(setOf("src/main/resources"))
    }
    test {
        kotlin.setSrcDirs(setOf("src/test/kotlin"))
        resources.setSrcDirs(setOf("src/test/resources"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
