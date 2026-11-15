import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.detekt)
}

version = providers.gradleProperty("version").getOrElse("dev")

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    allWarningsAsErrors = true
  }
}

springBoot {
  buildInfo {
    excludes = setOf("time")
  }
}

dependencies {
  implementation(platform(SpringBootPlugin.BOM_COORDINATES))
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-flyway")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation(libs.java.uuid.generator)
  implementation(libs.kotlin.logging)
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly(libs.imageio.webp)

  detektPlugins(libs.detekt.formatting)

  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-resttestclient")
  testImplementation("org.springframework.boot:spring-boot-jdbc-test")
  testImplementation(libs.kotest.assertions.core)
  testImplementation(libs.contracteer.verifier.junit)
  testImplementation(libs.archunit.junit5)
  testImplementation(libs.wiremock.standalone)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  testRuntimeOnly("org.testcontainers:testcontainers-postgresql")
}

detekt {
  buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
  jdkHome.convention(null as Directory?)
  jvmTarget = "21"
}

tasks.detekt {
  classpath.from(sourceSets.main.get().compileClasspath, sourceSets.main.get().output)
}

tasks.test {
  useJUnitPlatform()
  jvmArgs("-XX:TieredStopAtLevel=1")
  environmentFrom(".env.test")
  testLogging {
    events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    exceptionFormat = TestExceptionFormat.FULL
  }
}

tasks.bootRun {
  environmentFrom(".env")
}

fun dotenv(name: String): Map<String, String> = file(name).takeIf { it.isFile }?.readLines().orEmpty()
  .filter { it.isNotBlank() && !it.startsWith("#") }
  .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }

fun ProcessForkOptions.environmentFrom(name: String) =
  dotenv(name).forEach { (key, value) -> if (System.getenv(key) == null) environment(key, value) }
