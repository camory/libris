package fr.amory.libris.scenario

import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.Series
import fr.amory.libris.scenario.dsl.catalogue
import fr.amory.libris.scenario.dsl.hasAdded
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.client.RestTestClient

@ScenarioTest
class CatalogueScenarios @Autowired constructor(
  http: RestTestClient) {
  private val libris = Libris(http)

  @Test
  fun `S1 The catalogue lists the house's editions`() {
    // Given
    val lea = libris.reader("lea", "Léa")
    val sam = libris.reader("sam", "Sam")
    val leasBookshelf = lea.defaultBookshelf
    listOf(ONE_PIECE_2, EMILE, ASTERIX_ET_SES_AMIS, ONE_PIECE_1, ONE_PIECE_1, LA_SERPE_D_OR, ASTERIX_LE_GAULOIS)
      .forEach { lea.hasAdded(it, to = leasBookshelf) }
    sam.hasAdded(ONE_PIECE_3, to = sam.defaultBookshelf)

    // When
    val page = lea.catalogue()

    // Then
    page.books.map { it.title } shouldBe listOf(
      "Astérix le Gaulois",
      "La serpe d'or",
      "Astérix et ses amis",
      "Émile et les détectives",
      "Romance dawn",
      "Aux prises avec Baggy et ses hommes",
    )
    page.books[0].series shouldBe Series("Astérix", 1)
    page.books[2].series shouldBe Series("Astérix", null)
    page.books[3].series shouldBe null
    page.books[4].copies.map { it.bookshelf.name } shouldBe listOf("Bibliothèque de Léa", "Bibliothèque de Léa")
    page.books.flatMap { it.copies }.map { it.bookshelf.id } shouldBe List(7) { leasBookshelf.id }
    page.next shouldBe null
  }

  @Test
  fun `S2 The catalogue comes in pages`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    val bookshelf = marc.defaultBookshelf
    (1..51).forEach { marc.hasAdded(roman(it), to = bookshelf) }

    // When
    val firstPage = marc.catalogue()

    // Then
    firstPage.books.size shouldBe 50
    firstPage.books.first().title shouldBe "Roman 01"
    firstPage.books.last().title shouldBe "Roman 50"
    val secondPage = marc.catalogue(after = firstPage.next.shouldNotBeNull())
    secondPage.books.map { it.title } shouldBe listOf("Roman 51")
    secondPage.next shouldBe null
  }

  @Test
  fun `S3 The catalogue is empty`() {
    // Given
    val nina = libris.reader("nina", "Nina")

    // When
    val page = nina.catalogue()

    // Then
    page.books shouldBe emptyList()
    page.next shouldBe null
  }

  private companion object {
    val ONE_PIECE_1 = manga("9782723488525", "Romance dawn", "One piece", 1)
    val ONE_PIECE_2 = manga("9782723489898", "Aux prises avec Baggy et ses hommes", "One piece", 2)
    val ONE_PIECE_3 = manga("9782723489904", "Piège", "One piece", 3)
    val ASTERIX_LE_GAULOIS = bd("9782012101333", "Astérix le Gaulois", "Astérix", 1)
    val LA_SERPE_D_OR = bd("9782012101340", "La serpe d'or", "Astérix", 2)
    val ASTERIX_ET_SES_AMIS = bd("9782226174208", "Astérix et ses amis", "Astérix", null)
    val EMILE = book("9782013224895", "Émile et les détectives")

    fun manga(isbn: String, title: String, series: String, volume: Int) =
      NewBook(
        isbn13 = isbn,
        kind = "MANGA",
        title = title,
        authors = listOf(Author("Eiichirō Oda", "WRITER"), Author("Eiichirō Oda", "ARTIST")),
        series = Series(series, volume),
        language = "fr",
      )

    fun bd(isbn: String, title: String, series: String, volume: Int?) =
      NewBook(
        isbn13 = isbn,
        kind = "BD",
        title = title,
        authors = listOf(Author("René Goscinny", "WRITER"), Author("Albert Uderzo", "ARTIST")),
        series = Series(series, volume),
        language = "fr",
      )

    fun book(isbn: String, title: String) =
      NewBook(
        isbn13 = isbn,
        kind = "BOOK",
        title = title,
        authors = listOf(Author("Erich Kästner", "WRITER")),
        language = "fr",
      )

    fun roman(number: Int) =
      NewBook(
        isbn13 = isbn(number),
        kind = "BOOK",
        title = "Roman " + number.toString().padStart(2, '0'),
        authors = listOf(Author("Anonyme", "WRITER")),
        language = "fr",
      )

    fun isbn(number: Int): String {
      val body = "978200" + number.toString().padStart(6, '0')
      val sum = body.withIndex().sumOf { (i, c) -> c.digitToInt() * if (i % 2 == 0) 1 else 3 }
      return body + (10 - sum % 10) % 10
    }
  }
}
