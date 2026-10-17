package fr.amory.libris.scenario

import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NOT_FOUND
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.Role.VIEWER
import fr.amory.libris.scenario.dsl.SeriesEntry
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.hasCreated
import fr.amory.libris.scenario.dsl.opens
import fr.amory.libris.scenario.dsl.series
import fr.amory.libris.scenario.dsl.unknownSeries
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class SeriesPageScenarios @Autowired constructor(private val libris: Libris) {

  @Test
  @Disabled("series page")
  fun `S1 The reader opens a series`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    val salon = sam.hasCreated("Salon")
    sam.hasAdded(lea, VIEWER, to = salon)

    lea.hasAdded(ONE_PIECE_1, ONE_PIECE_2, NARUTO_1, to = lea.defaultBookshelf)
    sam.hasAdded(ONE_PIECE_2, ONE_PIECE_3, to = salon)
    sam.hasAdded(ONE_PIECE_4, to = sam.defaultBookshelf)

    // When
    val answer = lea.opens(lea.series().single { it.name == "One piece" })

    // Then
    val series = answer.value
    series.name shouldBe "One piece"
    series.books.map { it.title } shouldContainExactlyInAnyOrder listOf(
      "Romance dawn",
      "Aux prises avec Baggy et ses hommes",
      "Piège",
    )
  }

  @Test
  @Disabled("series page")
  fun `S5 No such series, a series Libris does not know`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val series = libris.unknownSeries()

    // When
    val answer = lea.opens(series)

    // Then
    answer.refusal shouldBe NOT_FOUND
  }

  @Test
  @Disabled("series page")
  fun `S5 No such series, one only on bookshelves the reader does not belong to`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    sam.hasAdded(ONE_PIECE_1, to = sam.defaultBookshelf)
    val series = sam.series().single { it.name == "One piece" }

    // When
    val answer = lea.opens(series)

    // Then
    answer.refusal shouldBe NOT_FOUND
  }

  private companion object {
    val ONE_PIECE_1 = manga("9782723488525", "Romance dawn", "One piece", "Eiichirō Oda", 1)
    val ONE_PIECE_2 = manga("9782723489898", "Aux prises avec Baggy et ses hommes", "One piece", "Eiichirō Oda", 2)
    val ONE_PIECE_3 = manga("9782723489904", "Piège", "One piece", "Eiichirō Oda", 3)
    val ONE_PIECE_4 = manga("9782723490221", "L'attaque au clair de lune", "One piece", "Eiichirō Oda", 4)
    val NARUTO_1 = manga("9782871294146", "Naruto Uzumaki", "Naruto", "Masashi Kishimoto", 1)

    fun manga(isbn: String, title: String, series: String, author: String, volume: Int) =
      NewBook(
        isbn13 = isbn,
        kind = "MANGA",
        title = title,
        authors = listOf(Author(author, "WRITER")),
        series = SeriesEntry(series, volume),
        language = "fr",
      )
  }
}
