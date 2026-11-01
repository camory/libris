package fr.amory.libris.bibliography.application.series

import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

class ListSeriesNamesTest {
  @Test
  fun `the name of every series of the house is answered`() {
    // Given
    val seriesRepository = SeriesInMemory().apply {
      findOrInsert(Series(SeriesId.new(), "One piece"))
      findOrInsert(Series(SeriesId.new(), "Astérix"))
    }
    val listSeriesNames = ListSeriesNames(seriesRepository)

    // When
    val names = listSeriesNames()

    // Then
    names shouldContainExactlyInAnyOrder listOf("One piece", "Astérix")
  }
}
