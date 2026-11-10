package fr.amory.libris.bibliography.application.series

import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.fixture.CarriedSeriesAnswering
import fr.amory.libris.bibliography.fixture.SeriesInMemory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ListSeriesNamesTest {
  @Test
  fun `the name of every series an edition carries is answered, none other`() {
    // Given
    val seriesRepository = SeriesInMemory()
    seriesRepository.findOrInsert(Series(SeriesId.new(), "One piece"))
    val asterix = seriesRepository.findOrInsert(Series(SeriesId.new(), "Astérix"))
    val listSeriesNames = ListSeriesNames(CarriedSeriesAnswering(setOf(asterix.id)), seriesRepository)

    // When
    val names = listSeriesNames()

    // Then
    names shouldBe listOf("Astérix")
  }
}
