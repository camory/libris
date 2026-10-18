package fr.amory.libris.bibliography.infrastructure.persistence

import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.fixture.JdbcSliceTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@JdbcSliceTest
@Import(JdbcSeriesRepository::class)
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
  fun `an id no series has finds none`() {
    seriesRepository.findById(SeriesId.new()) shouldBe null
  }
}
