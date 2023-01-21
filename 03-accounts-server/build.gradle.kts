import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
}

group = "io.github.alexswilliams"
version = "0.0.1"
application {
    mainClass.set("io.github.alexswilliams.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:_")
    implementation("io.ktor:ktor-server-host-common-jvm:_")
    implementation("io.ktor:ktor-server-cors-jvm:_")
    implementation("io.ktor:ktor-server-call-logging-jvm:_")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:_")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:_")
    implementation("io.ktor:ktor-server-netty-jvm:_")
    implementation("ch.qos.logback:logback-classic:_")
    testImplementation("io.ktor:ktor-server-tests-jvm:_")
    testImplementation(Kotlin.test.junit)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "19"
    }
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin")
        resources.srcDirs("src/main/resources")
    }
    test {
        kotlin.srcDirs("src/test/kotlin")
        resources.srcDirs("src/test/resources")
    }
}
