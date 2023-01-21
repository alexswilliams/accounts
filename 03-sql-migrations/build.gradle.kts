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
        kotlin.setSrcDirs(setOf("src/jvmMain/kotlin"))
        resources.setSrcDirs(setOf("src/jvmMain/resources", "../accounts-data/initial-transactions"))
    }
    test {
        kotlin.setSrcDirs(setOf("src/jvmTest/kotlin"))
        resources.setSrcDirs(setOf("src/jvmTest/resources"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
