package fr.amory.libris.scenario

import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.adds
import fr.amory.libris.scenario.dsl.asks
import fr.amory.libris.scenario.dsl.authorNames
import fr.amory.libris.scenario.dsl.hasAdded
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class AuthorsEditScenarios @Autowired constructor(private val libris: Libris) {
  private val lea = libris.reader("lea", "Léa")

  @Test
  @Disabled("authors edit")
  fun `S5 An author of the house is matched by name`() {
    // Given
    lea.hasAdded(LE_LOTUS_BLEU, to = lea.defaultBookshelf)

    // When
    lea.adds(LE_SCEPTRE_D_OTTOKAR.copy(authors = listOf(Author("herge ", "WRITER"))), to = lea.defaultBookshelf)

    // Then
    lea.asks(LE_SCEPTRE_D_OTTOKAR.isbn13).value.authors shouldBe listOf(Author("Hergé", "WRITER"))
    lea.authorNames() shouldBe listOf("Hergé")
  }

  @Test
  fun `S6 The authors of the house are suggested`() {
    // Given
    val sam = libris.reader("sam", "Sam")
    lea.hasAdded(ONE_PIECE_1, ONE_PIECE_2, to = lea.defaultBookshelf)
    sam.hasAdded(ASTERIX_LE_GAULOIS, to = sam.defaultBookshelf)

    // When
    val names = lea.authorNames()

    // Then
    names shouldContainExactlyInAnyOrder listOf("Eiichirō Oda", "René Goscinny", "Albert Uderzo")
  }

  private companion object {
    val LE_LOTUS_BLEU = tintin("9782203001046", "Le Lotus bleu")
    val LE_SCEPTRE_D_OTTOKAR = tintin("9782203001077", "Le Sceptre d'Ottokar")
    val ONE_PIECE_1 = onePiece("9782723488525", "Romance dawn")
    val ONE_PIECE_2 = onePiece("9782723489898", "Aux prises avec Baggy et ses hommes")
    val ASTERIX_LE_GAULOIS = NewBook(
      isbn13 = "9782012101333",
      kind = "BD",
      title = "Astérix le Gaulois",
      authors = listOf(Author("René Goscinny", "WRITER"), Author("Albert Uderzo", "ARTIST")),
      publisher = "Hachette",
      language = "fr",
    )

    fun tintin(isbn: String, title: String) =
      NewBook(
        isbn13 = isbn,
        kind = "BD",
        title = title,
        authors = listOf(Author("Hergé", "WRITER"), Author("Hergé", "ARTIST")),
        publisher = "Casterman",
        language = "fr",
      )

    fun onePiece(isbn: String, title: String) =
      NewBook(
        isbn13 = isbn,
        kind = "MANGA",
        title = title,
        authors = listOf(Author("Eiichirō Oda", "WRITER"), Author("Eiichirō Oda", "ARTIST")),
        publisher = "Glénat",
        language = "fr",
      )
  }
}
