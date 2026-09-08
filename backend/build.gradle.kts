import io.gitlab.arturbosch.detekt.Detekt
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        allWarningsAsErrors = true
    }
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    detektPlugins(libs.detekt.formatting)

    testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.kotest.assertions.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

detekt {
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    jdkHome.convention(null as Directory?)
    jvmTarget = "21"
}

tasks.detekt {
    dependsOn(tasks.compileKotlin)
    classpath.from(sourceSets.main.get().compileClasspath, sourceSets.main.get().output)
}

tasks.test {
    useJUnitPlatform()
}

tasks.check {
    dependsOn(tasks.koverXmlReport)
}
