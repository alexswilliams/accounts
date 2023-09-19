import de.fayard.refreshVersions.core.FeatureFlag.*
import de.fayard.refreshVersions.core.StabilityLevel

pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.10"
        id("io.ktor.plugin") version "2.3.4"
    }
}
plugins {
    id("de.fayard.refreshVersions") version "0.60.2"
    kotlin("jvm") apply false
    id("io.ktor.plugin") apply false
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel.isLessStableThan(current.stabilityLevel)
    }
    featureFlags {
        enable(VERSIONS_CATALOG)
        enable(GRADLE_UPDATES)
    }
}

rootProject.name = "accounts"

include("03-accounts-server")
include("03-migrations")
include("0x-nm-scraper")
