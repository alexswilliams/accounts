import de.fayard.refreshVersions.core.FeatureFlag.*

pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.10"
        id("io.ktor.plugin") version "2.2.4"
    }
}
plugins {
    id("de.fayard.refreshVersions") version "0.51.0"
    kotlin("jvm") apply false
    id("io.ktor.plugin") apply false
}

refreshVersions {
    featureFlags {
        enable(VERSIONS_CATALOG)
    }
}

rootProject.name = "accounts"

include("03-accounts-server")
include("03-migrations")
include("0x-nm-scraper")
