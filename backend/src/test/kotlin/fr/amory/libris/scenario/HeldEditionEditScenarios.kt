package fr.amory.libris.scenario

import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.bibliography.fixture.recordedBytes
import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Cover
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NOT_FOUND
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.SeriesEntry
import fr.amory.libris.scenario.dsl.adds
import fr.amory.libris.scenario.dsl.asks
import fr.amory.libris.scenario.dsl.authorNames
import fr.amory.libris.scenario.dsl.awaitsCoverOf
import fr.amory.libris.scenario.dsl.bookOf
import fr.amory.libris.scenario.dsl.catalogue
import fr.amory.libris.scenario.dsl.details
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.opens
import fr.amory.libris.scenario.dsl.saves
import fr.amory.libris.scenario.dsl.seriesNames
import fr.amory.libris.scenario.dsl.unknownBook
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class HeldEditionEditScenarios @Autowired constructor(
  private val libris: Libris,
  private val openLibrary: OpenLibraryStubs) {
  private val lea = libris.reader("lea", "Léa")

  @Test
  @Disabled("held edition edit")
  fun `S2 A held ISBN is added as edited`() {
    // Given
    val sam = libris.reader("sam", "Sam")
    openLibrary.hasCover(ONE_PIECE_1.isbn13, TALL_JPEG)
    sam.hasAdded(ONE_PIECE_1, to = sam.defaultBookshelf)
    val cover = sam.awaitsCoverOf(ONE_PIECE_1.isbn13)

    // When
    val answer = lea.adds(ONE_PIECE_1.copy(title = "À l'aube d'une grande aventure"), to = lea.defaultBookshelf)

    // Then
    val card = lea.asks(ONE_PIECE_1.isbn13).value
    card.title shouldBe "À l'aube d'une grande aventure"
    card.covers shouldBe listOf(Cover("Libris", cover))
    card.copies shouldBe listOf(answer.value)
  }

  @Test
  fun `S4 The ouvrage is saved as edited`() {
    // Given
    lea.hasAdded(ASTERIX_LE_GAULOIS_AS_STORED, to = lea.defaultBookshelf)
    val book = lea.catalogue().books.single()

    // When
    val answer = lea.saves(book, ASTERIX_LE_GAULOIS_AS_EDITED)

    // Then
    val page = lea.opens(book).value
    page.title shouldBe "Astérix le Gaulois"
    page.subtitle shouldBe "Une aventure d'Astérix"
    page.series shouldBe SeriesEntry("Astérix", 1)
    page.authors shouldBe listOf(Author("René Goscinny", "WRITER"), Author("Albert Uderzo", "ARTIST"))
    answer.value shouldBe page
  }

  @Test
  fun `S4 The ouvrage is saved as edited, its copies on another reader's bookshelf alone`() {
    // Given
    val sam = libris.reader("sam", "Sam")
    sam.hasAdded(ASTERIX_LE_GAULOIS_AS_STORED, to = sam.defaultBookshelf)
    val book = sam.catalogue().books.single()

    // When
    val answer = lea.saves(book, ASTERIX_LE_GAULOIS_AS_EDITED)

    // Then
    answer.value.title shouldBe "Astérix le Gaulois"
    answer.value.copies.shouldBeEmpty()
    val card = lea.asks(ASTERIX_LE_GAULOIS_AS_STORED.isbn13).value
    card.title shouldBe "Astérix le Gaulois"
    card.subtitle shouldBe "Une aventure d'Astérix"
    card.series shouldBe SeriesEntry("Astérix", 1)
    card.authors shouldBe listOf(Author("René Goscinny", "WRITER"), Author("Albert Uderzo", "ARTIST"))
  }

  @Test
  fun `S5 Its own name is respelled for the house, its series`() {
    // Given
    lea.hasAdded(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR, to = lea.defaultBookshelf)
    val book = lea.bookOf(ASTERIX_LE_GAULOIS.isbn13)

    // When
    lea.saves(book, ASTERIX_LE_GAULOIS.details.copy(series = SeriesEntry("asterix", 1)))

    // Then
    lea.asks(LA_SERPE_D_OR.isbn13).value.series shouldBe SeriesEntry("asterix", 2)
    lea.seriesNames() shouldBe listOf("asterix")
  }

  @Test
  fun `S5 Its own name is respelled for the house, another series of the house`() {
    // Given
    lea.hasAdded(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR, LE_LOTUS_BLEU, to = lea.defaultBookshelf)
    val book = lea.bookOf(LA_SERPE_D_OR.isbn13)

    // When
    lea.saves(book, LA_SERPE_D_OR.details.copy(series = SeriesEntry("tintin ", 2)))

    // Then
    lea.asks(LA_SERPE_D_OR.isbn13).value.series shouldBe SeriesEntry("Tintin", 2)
    lea.seriesNames() shouldContainExactlyInAnyOrder listOf("Astérix", "Tintin")
  }

  @Test
  @Disabled("held edition edit")
  fun `S5 Its own name is respelled for the house, its author`() {
    // Given
    lea.hasAdded(LE_LOTUS_BLEU, LE_SCEPTRE_D_OTTOKAR, to = lea.defaultBookshelf)
    val book = lea.bookOf(LE_LOTUS_BLEU.isbn13)

    // When
    lea.saves(book, LE_LOTUS_BLEU.details.copy(authors = listOf(Author("herge", "WRITER"), Author("herge", "ARTIST"))))

    // Then
    lea.asks(LE_SCEPTRE_D_OTTOKAR.isbn13).value.authors shouldBe
      listOf(Author("herge", "WRITER"), Author("herge", "ARTIST"))
    lea.authorNames() shouldBe listOf("herge")
  }

  @Test
  fun `S5 Its own name is respelled for the house, another author of the house`() {
    // Given
    lea.hasAdded(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR.copy(authors = emptyList()), to = lea.defaultBookshelf)
    val book = lea.bookOf(LA_SERPE_D_OR.isbn13)

    // When
    lea.saves(book, LA_SERPE_D_OR.details.copy(authors = listOf(Author("rene goscinny", "WRITER"))))

    // Then
    lea.asks(LA_SERPE_D_OR.isbn13).value.authors shouldBe listOf(Author("René Goscinny", "WRITER"))
  }

  @Test
  @Disabled("held edition edit")
  fun `S5 Its own name is respelled for the house, the add of a held ISBN`() {
    // Given
    val sam = libris.reader("sam", "Sam")
    sam.hasAdded(ASTERIX_LE_GAULOIS, to = sam.defaultBookshelf)

    // When
    lea.adds(ASTERIX_LE_GAULOIS.copy(series = SeriesEntry("asterix", 1)), to = lea.defaultBookshelf)

    // Then
    lea.seriesNames() shouldBe listOf("asterix")
  }

  @Test
  fun `S6 A name no edition carries is not suggested`() {
    // Given
    lea.hasAdded(ASTERIX_LE_GAULOIS, to = lea.defaultBookshelf)
    val book = lea.catalogue().books.single()

    // When
    lea.saves(
      book,
      ASTERIX_LE_GAULOIS.details.copy(
        series = SeriesEntry("Tintin", 1),
        authors = listOf(Author("Albert Uderzo", "ARTIST")),
      ),
    )

    // Then
    lea.seriesNames() shouldBe listOf("Tintin")
    lea.authorNames() shouldBe listOf("Albert Uderzo")
  }

  @Test
  fun `S7 An edition Libris does not know is not saved`() {
    // Given
    val book = libris.unknownBook()

    // When
    val answer = lea.saves(book, ASTERIX_LE_GAULOIS.details.copy(series = SeriesEntry("Tintin", 1)))

    // Then
    answer.refusal shouldBe NOT_FOUND
    lea.seriesNames().shouldBeEmpty()
  }

  private companion object {
    val TALL_JPEG = recordedBytes("covers/tall.jpg")
    val ONE_PIECE_1 = NewBook(
      isbn13 = "9782723488525",
      kind = "MANGA",
      title = "Romance dawn",
      authors = listOf(Author("Eiichirō Oda", "WRITER"), Author("Eiichirō Oda", "ARTIST")),
      series = SeriesEntry("One piece", 1),
      publisher = "Glénat",
      language = "fr",
      coverSource = "Open Library",
    )
    val ASTERIX_LE_GAULOIS_AS_STORED = NewBook(
      isbn13 = "9782012101333",
      kind = "BD",
      title = "Asterix le gaulois",
      authors = listOf(Author("René Goscinny", "WRITER")),
      publisher = "Hachette",
      language = "fr",
    )
    val ASTERIX_LE_GAULOIS_AS_EDITED = ASTERIX_LE_GAULOIS_AS_STORED.details.copy(
      title = "Astérix le Gaulois",
      subtitle = "Une aventure d'Astérix",
      series = SeriesEntry("Astérix", 1),
      authors = listOf(Author("René Goscinny", "WRITER"), Author("Albert Uderzo", "ARTIST")),
    )
    val ASTERIX_LE_GAULOIS = asterix("9782012101333", "Astérix le Gaulois", 1)
    val LA_SERPE_D_OR = asterix("9782012101340", "La serpe d'or", 2)
    val LE_LOTUS_BLEU = tintin("9782203001046", "Le Lotus bleu", 5)
    val LE_SCEPTRE_D_OTTOKAR = tintin("9782203001077", "Le Sceptre d'Ottokar", 8)

    fun asterix(isbn: String, title: String, volume: Int) =
      NewBook(
        isbn13 = isbn,
        kind = "BD",
        title = title,
        authors = listOf(Author("René Goscinny", "WRITER"), Author("Albert Uderzo", "ARTIST")),
        series = SeriesEntry("Astérix", volume),
        publisher = "Hachette",
        language = "fr",
      )

    fun tintin(isbn: String, title: String, volume: Int) =
      NewBook(
        isbn13 = isbn,
        kind = "BD",
        title = title,
        authors = listOf(Author("Hergé", "WRITER"), Author("Hergé", "ARTIST")),
        series = SeriesEntry("Tintin", volume),
        publisher = "Casterman",
        language = "fr",
      )
  }
}
