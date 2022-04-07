val kotlinVersion = "1.6.20"

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "me.alex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation("io.klogging:klogging-jvm:0.4.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    runtimeOnly("org.postgresql:postgresql:42.3.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
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
        check(this is JavaToolchainSpec)
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
