import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        resources.setSrcDirs(setOf("src/main/resources", "../secrets/0x-nm-scraper"))
    }
    test {
        kotlin.setSrcDirs(setOf("src/test/kotlin"))
        resources.setSrcDirs(setOf("src/test/resources"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
