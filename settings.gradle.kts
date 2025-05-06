pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val refreshVersionsVersion: String by settings

    plugins {
        id("de.fayard.refreshVersions") version refreshVersionsVersion
    }
}

plugins {
    id("de.fayard.refreshVersions")
}

rootProject.name = "external-dependencies"
