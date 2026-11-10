package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import fr.amory.libris.fixture.JdbcSliceTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@JdbcSliceTest
class JdbcCarriedSeriesTest @Autowired constructor(
  private val carriedSeries: JdbcCarriedSeries,
  private val editions: JdbcEditionRepository,
  private val seriesRepository: JdbcSeriesRepository) {
  @Test
  fun `the series an edition is in is found, not another of the house`() {
    // Given
    val asterix = seriesRepository.findOrInsert(Series(SeriesId.new(), "Astérix"))
    seriesRepository.findOrInsert(Series(SeriesId.new(), "Tintin"))
    editions.insert(edition(SeriesVolume(asterix.id, 1)))

    // When
    val found = carriedSeries.findAll()

    // Then
    found shouldBe setOf(asterix.id)
  }

  @Test
  fun `an edition without a series carries none`() {
    // Given
    editions.insert(edition(seriesVolume = null))

    // When
    val found = carriedSeries.findAll()

    // Then
    found shouldBe emptySet()
  }
}

private fun edition(seriesVolume: SeriesVolume?): Edition =
  Edition(
    id = EditionId.new(),
    isbn = null,
    kind = BD,
    title = "Astérix le Gaulois",
    subtitle = null,
    contributions = Contributions.of(emptyList()),
    seriesVolume = seriesVolume,
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverName = null,
  )
