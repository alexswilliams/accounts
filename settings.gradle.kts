plugins {
    id("de.fayard.refreshVersions") version "0.40.2"
}

rootProject.name = "accounts"

include("03-sql-migrations")
include("0x-nm-scraper")

