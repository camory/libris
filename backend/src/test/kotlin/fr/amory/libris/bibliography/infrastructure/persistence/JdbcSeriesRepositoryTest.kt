package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.fixture.JdbcSliceTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@JdbcSliceTest
class JdbcSeriesRepositoryTest @Autowired constructor(private val seriesRepository: JdbcSeriesRepository) {
  @Test
  fun `a series new to the house is stored under its name`() {
    // Given
    val series = Series(SeriesId.new(), "One piece")

    // When
    val stored = seriesRepository.findOrInsert(series)

    // Then
    stored shouldBe series
    seriesRepository.findById(series.id) shouldBe series
  }

  @Test
  fun `a name the house holds, in another capitalisation, answers the series stored`() {
    // Given
    val first = Series(SeriesId.new(), "One piece")
    seriesRepository.findOrInsert(first)
    val again = Series(SeriesId.new(), "ONE PIECE")

    // When
    val stored = seriesRepository.findOrInsert(again)

    // Then
    stored shouldBe first
    seriesRepository.findById(again.id) shouldBe null
  }

  @Test
  fun `a name the house holds, in other accents, answers the series stored`() {
    // Given
    val first = Series(SeriesId.new(), "Astérix")
    seriesRepository.findOrInsert(first)
    val again = Series(SeriesId.new(), "Asterix")

    // When
    val stored = seriesRepository.findOrInsert(again)

    // Then
    stored shouldBe first
    seriesRepository.findById(again.id) shouldBe null
  }

  @Test
  fun `a name the house holds, with spaces at its ends, answers the series stored`() {
    // Given
    val first = Series(SeriesId.new(), "Astérix")
    seriesRepository.findOrInsert(first)
    val again = Series(SeriesId.new(), " Astérix ")

    // When
    val stored = seriesRepository.findOrInsert(again)

    // Then
    stored shouldBe first
    seriesRepository.findById(again.id) shouldBe null
  }

  @Test
  fun `a name matching the own series, in other capitals and accents, respells it`() {
    // Given
    val asterix = seriesRepository.findOrInsert(Series(SeriesId.new(), "Astérix"))
    val given = Series(SeriesId.new(), "asterix")

    // When
    val stored = seriesRepository.respellOrFindOrInsert(given, ownSeriesId = asterix.id)

    // Then
    stored shouldBe Series(asterix.id, "asterix")
    seriesRepository.findById(asterix.id) shouldBe Series(asterix.id, "asterix")
    seriesRepository.findById(given.id) shouldBe null
  }

  @Test
  fun `an id no series has finds none`() {
    seriesRepository.findById(SeriesId.new()) shouldBe null
  }

  @Test
  fun `the series of the given ids are read back, none other`() {
    // Given
    val onePiece = seriesRepository.findOrInsert(Series(SeriesId.new(), "One piece"))
    val tintin = seriesRepository.findOrInsert(Series(SeriesId.new(), "Tintin"))
    seriesRepository.findOrInsert(Series(SeriesId.new(), "Naruto"))

    // When
    val found = seriesRepository.findByIds(listOf(onePiece.id, tintin.id))

    // Then
    found shouldContainExactlyInAnyOrder listOf(onePiece, tintin)
  }

  @Test
  fun `no id finds no series`() {
    // Given
    seriesRepository.findOrInsert(Series(SeriesId.new(), "One piece"))

    // When
    val found = seriesRepository.findByIds(emptyList())

    // Then
    found shouldBe emptyList()
  }
}
