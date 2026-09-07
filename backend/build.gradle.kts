import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

group = "fr.amory"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.flyway.postgresql)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.web.server.test)
    testImplementation(libs.spring.boot.resttestclient)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.kotest.assertions.core)

    detektPlugins(libs.detekt.formatting)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Werror")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("config/detekt/detekt.yml")
}

kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}
