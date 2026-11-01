package fr.amory.libris.fixture

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration(proxyBeanMethods = false)
class CleanSchema {
  @Bean
  fun cleanThenMigrate() =
    FlywayMigrationStrategy {
      it.clean()
      it.migrate()
    }
}
