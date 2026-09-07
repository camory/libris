import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kover)
}

group = "fr.amory"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val detekt = configurations.create("detekt")
val detektPlugins = configurations.create("detektPlugins")

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

    detekt(libs.detekt.cli)
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

// detekt 1.23.8 embeds the Kotlin 2.0.21 compiler, which refuses to start on a
// JDK newer than 24, so its Gradle plugin cannot run inside this JDK 25 build.
// The CLI runs instead, on a JDK 21 toolchain provisioned by the foojay
// resolver (settings.gradle.kts), with the same configuration the plugin would
// have used: the default config plus config/detekt/detekt.yml, the formatting
// ruleset as a plugin, main and test sources.
val detektSources = files("src/main/kotlin", "src/test/kotlin")
val detektConfig = layout.projectDirectory.file("config/detekt/detekt.yml")
val detektReport = layout.buildDirectory.file("reports/detekt/detekt.html")

tasks.register<JavaExec>("detekt") {
    description = "Runs detekt with the formatting ruleset on main and test sources (D07, D10)."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(21) }
    classpath = detekt
    mainClass = "io.gitlab.arturbosch.detekt.cli.Main"
    inputs.files(detektSources).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(detektConfig).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(rootDir.resolve("../.editorconfig")).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.files(detektPlugins)
    outputs.file(detektReport)
    argumentProviders.add {
        listOf(
            "--build-upon-default-config",
            "--config", detektConfig.asFile.path,
            "--input", detektSources.joinToString(",") { it.path },
            "--plugins", detektPlugins.joinToString(",") { it.path },
            "--report", "html:${detektReport.get().asFile.path}",
        )
    }
}

tasks.check {
    dependsOn("detekt")
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
