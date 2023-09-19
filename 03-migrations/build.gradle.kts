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
    implementation(libs.jackson)
    implementation(libs.commons.compress)

    runtimeOnly(libs.logback.classic)

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
