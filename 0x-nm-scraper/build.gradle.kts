val kotlinVersion = "1.8.0"

plugins {
    kotlin("jvm")
    application
}

group = "me.alex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    testImplementation(Testing.junit.jupiter)
    testImplementation(Kotlin.test.junit)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "19"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "19"
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }

    sourceSets {
        main {
            kotlin.srcDir("src/jvmMain/kotlin")
        }
        test {
            kotlin.srcDir("src/jvmTest/kotlin")
        }
    }
}
sourceSets {
    main {
        resources.srcDirs("src/jvmMain/resources", "../secrets/0x-nm-scraper")
    }
    test {
        resources.srcDir("src/jvmTest/resources")
    }
}

application {
    mainClass.set("me.alex.application.MainKt")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
