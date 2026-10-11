// Repository da cui Gradle risolve i plugin; i filtri limitano i gruppi cercati su Google.
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
// Resolver dei JDK per le toolchain Gradle.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
// Repository centralizzati per le librerie; i moduli non possono definirne altri.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Perudo"
// Unico modulo applicativo Android del progetto.
include(":app")
 