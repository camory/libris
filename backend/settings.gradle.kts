pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// Provisions the JDK 21 toolchain that the detekt task runs on (see build.gradle.kts).
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "libris-backend"
