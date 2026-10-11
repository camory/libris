package fr.amory.libris.scenario

import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Card
import fr.amory.libris.scenario.dsl.Cover
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NOT_FOUND
import fr.amory.libris.scenario.dsl.SOURCES_UNAVAILABLE
import fr.amory.libris.scenario.dsl.Series
import fr.amory.libris.scenario.dsl.asks
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class FastEntryScenarios @Autowired constructor(
  libris: Libris,
  private val bnf: BnfStubs,
  private val openLibrary: OpenLibraryStubs) {
  private val juliette = libris.reader("juliette", "Juliette")

  @Test
  fun `S1 Typed ISBN, found`() {
    // Given
    bnf.knows("9782723488525")

    // When
    val answer = juliette.asks("9782723488525")

    // Then
    answer.value shouldBe Card(
      id = null,
      isbn13 = "9782723488525",
      kind = "MANGA",
      title = "Romance dawn",
      subtitle = "à l'aube d'une grande aventure",
      authors = listOf(Author("Eiichirō Oda", "WRITER")),
      series = Series("One piece", 1),
      collection = "Shonen manga",
      publisher = "Glénat",
      publicationYear = 2013,
      language = "fr",
      pageCount = 203,
      summary = null,
      covers = listOf(Cover("BnF", ONE_PIECE_COVER)),
      copies = emptyList(),
    )
  }

  @Test
  fun `S4 Unknown ISBN`() {
    // Given
    bnf.doesNotKnow("9782000000013")
    openLibrary.doesNotKnow("9782000000013")

    // When
    val answer = juliette.asks("9782000000013")

    // Then
    answer.refusal shouldBe NOT_FOUND
  }

  @Test
  fun `S5 Merged answer`() {
    // Given
    bnf.partiallyKnows("9782723488525")
    openLibrary.knows("9782723488525")

    // When
    val answer = juliette.asks("9782723488525")

    // Then
    val card = answer.value
    card.title shouldBe "Romance dawn"
    card.authors.first().name shouldBe "Eiichirō Oda"
    card.pageCount shouldBe 207
    card.publicationYear shouldBe 2013
  }

  @Test
  fun `S5 Merged answer, from Open Library alone`() {
    // Given
    bnf.doesNotKnow("9782380751673")
    openLibrary.knows("9782380751673")

    // When
    val answer = juliette.asks("9782380751673")

    // Then
    val card = answer.value
    card.title shouldBe "Space Wars - Chapitre 1"
    card.authors.map { it.name } shouldBe listOf("Baba", "Stéphane Lapuss'", "Tartuff")
    card.publisher shouldBe "KENNES EDITIONS"
  }

  @Test
  fun `S6 One source down`() {
    // Given
    bnf.fails()
    openLibrary.knows("9782380751673")

    // When
    val answer = juliette.asks("9782380751673")

    // Then
    answer.value.title shouldBe "Space Wars - Chapitre 1"
  }

  @Test
  fun `S6 One source down, past the timeout`() {
    // Given
    bnf.knows("9782723488525")
    openLibrary.answersTooLate("9782723488525")

    // When
    val answer = juliette.asks("9782723488525")

    // Then
    answer.value.title shouldBe "Romance dawn"
  }

  @Test
  fun `S7 Every source down`() {
    // Given
    bnf.fails()
    openLibrary.fails()

    // When
    val answer = juliette.asks("9782723488525")

    // Then
    answer.refusal shouldBe SOURCES_UNAVAILABLE
  }

  @Test
  fun `S7 Every source down, past the timeout`() {
    // Given
    bnf.answersTooLate("9782723488525")
    openLibrary.fails()

    // When
    val answer = juliette.asks("9782723488525")

    // Then
    answer.refusal shouldBe SOURCES_UNAVAILABLE
  }

  private companion object {
    const val ONE_PIECE_COVER =
      "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb43636708p&couverture=1"
  }
}
