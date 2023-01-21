import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

group = "io.github.alexswilliams"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(Testing.junit.jupiter)
    testImplementation(Kotlin.test.junit)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "19"
    }
}
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

sourceSets {
    main {
        kotlin.srcDir("src/jvmMain/kotlin")
        resources.srcDirs("src/jvmMain/resources", "../secrets/0x-nm-scraper")
    }
    test {
        kotlin.srcDir("src/jvmTest/kotlin")
        resources.srcDir("src/jvmTest/resources")
    }
}

application {
    mainClass.set("me.alex.application.MainKt")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
