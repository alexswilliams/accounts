val kotlinVersion = "1.7.0"

plugins {
    kotlin("jvm") version "1.7.0"
    application
}

group = "me.alex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
    runtimeOnly("ch.qos.logback:logback-classic:1.3.0-alpha16")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.apache.commons:commons-compress:1.21")

    runtimeOnly("org.postgresql:postgresql:42.4.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
        resources.srcDirs("src/jvmMain/resources", "../secrets/02-data", "../secrets/03-resources")
    }
    test {
        resources.srcDir("src/jvmTest/resources")
    }
}

application {
    mainClass.set("me.alex.application.SqlMigrationsMain")
}
