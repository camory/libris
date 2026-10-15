package fr.amory.libris.scenario

import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.Role.VIEWER
import fr.amory.libris.scenario.dsl.SeriesEntry
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.hasCreated
import fr.amory.libris.scenario.dsl.series
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class SeriesListScenarios @Autowired constructor(private val libris: Libris) {

  @Test
  fun `S1 The reader's series`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    val salon = sam.hasCreated("Salon")
    sam.hasAdded(lea, VIEWER, to = salon)

    lea.hasAdded(ONE_PIECE_1, BLACKSAD_1, EMILE_ET_MARGOT_1, to = lea.defaultBookshelf)
    sam.hasAdded(ONE_PIECE_2, ASTERIX_1, to = salon)
    sam.hasAdded(NARUTO_1, to = sam.defaultBookshelf)
    lea.hasAdded(EMILE, to = lea.defaultBookshelf)

    // When
    val series = lea.series()

    // Then
    series.map { it.name } shouldContainExactlyInAnyOrder listOf(
      "Astérix",
      "blacksad",
      "Émile et Margot",
      "One piece",
    )
  }

  @Test
  fun `S5 No series`() {
    // Given
    val nina = libris.reader("nina", "Nina")
    nina.hasAdded(EMILE, LE_PETIT_PRINCE, to = nina.defaultBookshelf)

    // When
    val series = nina.series()

    // Then
    series shouldBe emptyList()
  }

  private companion object {
    val ONE_PIECE_1 = manga("9782723488525", "Romance dawn", "One piece", "Eiichirō Oda")
    val ONE_PIECE_2 = manga("9782723489898", "Aux prises avec Baggy et ses hommes", "One piece", "Eiichirō Oda", 2)
    val NARUTO_1 = manga("9782871294146", "Naruto Uzumaki", "Naruto", "Masashi Kishimoto")
    val ASTERIX_1 = bd("9782012101333", "Astérix le Gaulois", "Astérix", "René Goscinny")
    val BLACKSAD_1 = bd("9782205049213", "Quelque part entre les ombres", "blacksad", "Juan Díaz Canales")
    val EMILE_ET_MARGOT_1 = bd("9782205062793", "Interdit aux monstres", "Émile et Margot", "Didier Lévy")
    val EMILE = book("9782013224895", "Émile et les détectives", "Erich Kästner")
    val LE_PETIT_PRINCE = book("9782070612758", "Le Petit Prince", "Antoine de Saint-Exupéry")

    fun manga(isbn: String, title: String, series: String, author: String, volume: Int = 1) =
      NewBook(
        isbn13 = isbn,
        kind = "MANGA",
        title = title,
        authors = listOf(Author(author, "WRITER")),
        series = SeriesEntry(series, volume),
        language = "fr",
      )

    fun bd(isbn: String, title: String, series: String, author: String) =
      NewBook(
        isbn13 = isbn,
        kind = "BD",
        title = title,
        authors = listOf(Author(author, "WRITER")),
        series = SeriesEntry(series, 1),
        language = "fr",
      )

    fun book(isbn: String, title: String, author: String) =
      NewBook(
        isbn13 = isbn,
        kind = "BOOK",
        title = title,
        authors = listOf(Author(author, "WRITER")),
        language = "fr",
      )
  }
}
