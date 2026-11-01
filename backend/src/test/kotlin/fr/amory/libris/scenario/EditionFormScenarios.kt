package fr.amory.libris.scenario

import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.SeriesEntry
import fr.amory.libris.scenario.dsl.adds
import fr.amory.libris.scenario.dsl.asks
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.series
import fr.amory.libris.scenario.dsl.seriesNames
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class EditionFormScenarios @Autowired constructor(private val libris: Libris, private val bnf: BnfStubs) {
  private val lea = libris.reader("lea", "Léa")

  @Test
  fun `S5 The ouvrage is added as edited`() {
    // Given
    bnf.knows(MONT_TOMBE)
    lea.asks(MONT_TOMBE).value.copies shouldBe emptyList()

    // When
    val answer = lea.adds(MONT_TOMBE_AS_EDITED, to = lea.defaultBookshelf)

    // Then
    val copy = answer.value
    copy.bookshelf.name shouldBe "Bibliothèque de Léa"
    val card = lea.asks(MONT_TOMBE).value
    card.title shouldBe "La dernière légende du Mont-Saint-Michel"
    card.series shouldBe SeriesEntry("Mont Tombe", 1)
    card.copies shouldBe listOf(copy)
  }

  @Test
  fun `S5 The ouvrage is added as edited, a series of the house`() {
    // Given
    lea.hasAdded(ASTERIX_LE_GAULOIS, to = lea.defaultBookshelf)

    // When
    lea.adds(LA_SERPE_D_OR.copy(series = SeriesEntry("asterix ", 2)), to = lea.defaultBookshelf)

    // Then
    lea.asks(LA_SERPE_D_OR.isbn13).value.series shouldBe SeriesEntry("Astérix", 2)
    lea.series().map { it.name } shouldBe listOf("Astérix")
  }

  @Test
  fun `S7 The series of the house are suggested`() {
    // Given
    val sam = libris.reader("sam", "Sam")
    lea.hasAdded(ONE_PIECE_1, ONE_PIECE_2, to = lea.defaultBookshelf)
    sam.hasAdded(ASTERIX_LE_GAULOIS, to = sam.defaultBookshelf)

    // When
    val names = lea.seriesNames()

    // Then
    names shouldContainExactlyInAnyOrder listOf("One piece", "Astérix")
  }

  private companion object {
    const val MONT_TOMBE = "9782344008980"
    val MONT_TOMBE_AS_EDITED = NewBook(
      isbn13 = MONT_TOMBE,
      kind = "BD",
      title = "La dernière légende du Mont-Saint-Michel",
      series = SeriesEntry("Mont Tombe", 1),
      collection = "Seinen manga",
      publisher = "Glénat",
      publicationYear = 2015,
      language = "fr",
    )
    val ONE_PIECE_1 = manga("9782723488525", "Romance dawn", 1)
    val ONE_PIECE_2 = manga("9782723489898", "Aux prises avec Baggy et ses hommes", 2)
    val ASTERIX_LE_GAULOIS = bd("9782012101333", "Astérix le Gaulois", "Astérix", 1)
    val LA_SERPE_D_OR = bd("9782012101340", "La serpe d'or", "Astérix", 2)

    fun manga(isbn: String, title: String, volume: Int) =
      NewBook(
        isbn13 = isbn,
        kind = "MANGA",
        title = title,
        series = SeriesEntry("One piece", volume),
        publisher = "Glénat",
        language = "fr",
      )

    fun bd(isbn: String, title: String, series: String, volume: Int) =
      NewBook(
        isbn13 = isbn,
        kind = "BD",
        title = title,
        series = SeriesEntry(series, volume),
        publisher = "Hachette",
        language = "fr",
      )
  }
}
