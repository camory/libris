package fr.amory.libris.fixture

import fr.amory.libris.bibliography.infrastructure.persistence.JdbcAuthorNames
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcAwaitedCoverRepository
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcCarriedSeries
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcEditionRepository
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcSeriesRepository
import fr.amory.libris.library.infrastructure.persistence.JdbcBookshelfRepository
import fr.amory.libris.library.infrastructure.persistence.JdbcCatalogueEditions
import fr.amory.libris.library.infrastructure.persistence.JdbcCopyRepository
import fr.amory.libris.library.infrastructure.persistence.JdbcReaderRepository
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@JdbcTest(properties = ["spring.flyway.clean-disabled=false"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
@Import(
  CleanSchema::class,
  JdbcAuthorNames::class,
  JdbcAwaitedCoverRepository::class,
  JdbcCarriedSeries::class,
  JdbcEditionRepository::class,
  JdbcSeriesRepository::class,
  JdbcBookshelfRepository::class,
  JdbcCatalogueEditions::class,
  JdbcCopyRepository::class,
  JdbcReaderRepository::class,
)
@ExtendWith(FreshSchema::class)
annotation class JdbcSliceTest
