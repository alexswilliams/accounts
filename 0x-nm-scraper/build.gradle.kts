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
        kotlin.setSrcDirs(setOf("src/jvmMain/kotlin"))
        resources.setSrcDirs(setOf("src/jvmMain/resources", "../secrets/0x-nm-scraper"))
    }
    test {
        kotlin.setSrcDirs(setOf("src/jvmTest/kotlin"))
        resources.setSrcDirs(setOf("src/jvmTest/resources"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
