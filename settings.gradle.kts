pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    plugins {
        id("de.fayard.refreshVersions") version "0.40.2"
////                                # available:"0.50.0"
////                                # available:"0.50.1"
////                                # available:"0.50.2"
////                                # available:"0.51.0"
        kotlin("jvm") version "1.8.0" apply false
        id("io.ktor.plugin") version "2.2.2" apply false
    }
}
plugins {
    id("de.fayard.refreshVersions")
    kotlin("jvm") apply false
    id("io.ktor.plugin") apply false
}

rootProject.name = "accounts"

include("03-accounts-server")
include("03-sql-migrations")
include("0x-nm-scraper")

