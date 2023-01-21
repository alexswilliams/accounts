@Suppress("DSL_SCOPE_VIOLATION") // suppress intellij 2022.3 bug
plugins {
    alias(libs.plugins.kotlin)
}

group = "io.github.alexswilliams"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.commons.compress)

    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
}


val jvmVersion: String by project
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
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
        resources.setSrcDirs(setOf("src/main/resources", "../accounts-data/from-google-sheets"))
    }
    test {
        kotlin.setSrcDirs(setOf("src/test/kotlin"))
        resources.setSrcDirs(setOf("src/test/resources"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
