val kotlinVersion = "1.7.0"

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
    implementation("io.github.microutils:kotlin-logging-jvm:_")
    runtimeOnly("ch.qos.logback:logback-classic:_")

    implementation(KotlinX.serialization.json)
    implementation("org.apache.commons:commons-compress:_")

    runtimeOnly("org.postgresql:postgresql:_")

    testImplementation(Kotlin.test.junit)
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
