package fr.amory.libris.scenario

import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.SeriesEntry
import fr.amory.libris.scenario.dsl.bookOf
import fr.amory.libris.scenario.dsl.catalogueRowsOf
import fr.amory.libris.scenario.dsl.details
import fr.amory.libris.scenario.dsl.hasAdded
import fr.amory.libris.scenario.dsl.hasSaved
import fr.amory.libris.scenario.dsl.searches
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.FieldSource
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class SearchScenarios @Autowired constructor(private val libris: Libris) {
  private val lea = libris.reader("lea", "Léa")

  @Disabled("search")
  @ParameterizedTest(name = "S1 Words find the editions holding them, {argumentSetName}")
  @FieldSource("WORDS")
  fun `S1 Words find the editions holding them`(words: String, titles: List<String>) {
    // Given
    val sam = libris.reader("sam", "Sam")
    val leasBookshelf = lea.defaultBookshelf
    lea.hasAdded(ASTERIX_ET_LES_GOTHS, LA_SERPE_D_OR, ONE_PIECE_20, ONE_PIECE_2, ONE_PIECE_12, to = leasBookshelf)
    sam.hasAdded(LA_SERPE_D_OR, LE_LOTUS_BLEU, to = sam.defaultBookshelf)

    // When
    val answer = lea.searches(words)

    // Then
    answer.books shouldBe lea.catalogueRowsOf(titles)
    answer.near shouldBe false
  }

  @ParameterizedTest(name = "S2 A whole ISBN finds its edition, {argumentSetName}")
  @FieldSource("ISBNS")
  fun `S2 A whole ISBN finds its edition`(isbn: String, titles: List<String>) {
    // Given
    lea.hasAdded(ASTERIX_ET_LES_GOTHS, LA_SERPE_D_OR, ONE_PIECE_2, to = lea.defaultBookshelf)

    // When
    val answer = lea.searches(isbn)

    // Then
    answer.books shouldBe lea.catalogueRowsOf(titles)
    answer.near shouldBe false
  }

  @Disabled("search")
  @ParameterizedTest(name = "S3 Near matches when the words find nothing, {argumentSetName}")
  @FieldSource("MISSPELLED")
  fun `S3 Near matches when the words find nothing`(words: String, titles: List<String>, near: Boolean) {
    // Given
    lea.hasAdded(ASTERIX_ET_LES_GOTHS, LA_SERPE_D_OR, ONE_PIECE_2, to = lea.defaultBookshelf)

    // When
    val answer = lea.searches(words)

    // Then
    answer.books shouldBe lea.catalogueRowsOf(titles)
    answer.near shouldBe near
  }

  @Disabled("search")
  @Test
  fun `S4 Results come in pages`() {
    // Given
    lea.hasAdded(EMILE, to = lea.defaultBookshelf)
    (1..51).forEach { lea.hasAdded(roman(it), to = lea.defaultBookshelf) }

    // When
    val firstPage = lea.searches("roman")

    // Then
    firstPage.books.size shouldBe 50
    firstPage.books.first().title shouldBe "Roman 01"
    firstPage.books.last().title shouldBe "Roman 50"
    val secondPage = lea.searches("roman", after = firstPage.next.shouldNotBeNull())
    secondPage.books.map { it.title } shouldBe listOf("Roman 51")
    secondPage.next shouldBe null
  }

  @Test
  fun `S5 An edition is found by what it holds now, an edition added`() {
    // Given
    lea.hasAdded(LA_SERPE_D_OR, ONE_PIECE_2, to = lea.defaultBookshelf)

    // When
    val answer = lea.searches("serpe")

    // Then
    answer.books shouldBe lea.catalogueRowsOf(listOf("La serpe d'or"))
  }

  @Disabled("search")
  @Test
  fun `S5 An edition is found by what it holds now, an edition whose title a reader changed`() {
    // Given
    lea.hasAdded(ONE_PIECE_1, ONE_PIECE_2, to = lea.defaultBookshelf)
    lea.hasSaved(lea.bookOf(ONE_PIECE_1.isbn13), ONE_PIECE_1.details.copy(title = "À l'aube d'une grande aventure"))

    // When
    val byNewTitle = lea.searches("grande aventure")
    val byOldTitle = lea.searches("romance dawn")

    // Then
    byNewTitle.books shouldBe lea.catalogueRowsOf(listOf("À l'aube d'une grande aventure"))
    byOldTitle.books.shouldBeEmpty()
  }

  private companion object {
    val WORDS = listOf(
      argumentSet("a word", "asterix", listOf("La serpe d'or", "Astérix et les Goths")),
      argumentSet("a word begun", "ast", listOf("La serpe d'or", "Astérix et les Goths")),
      argumentSet("words in two fields", "uderzo serpe", listOf("La serpe d'or")),
      argumentSet("a title typed whole", "la serpe d'or", listOf("La serpe d'or")),
      argumentSet("a tome", "one piece 2", listOf("Aux prises avec Baggy et ses hommes")),
      argumentSet("an edition on another reader's bookshelf alone", "lotus bleu", emptyList<String>()),
    )

    val ISBNS = listOf(
      argumentSet("the thirteen digits", "9782012101340", listOf("La serpe d'or")),
      argumentSet("the thirteen digits with hyphens", "978-2-01-210134-0", listOf("La serpe d'or")),
      argumentSet("the ISBN-10", "2012101348", listOf("La serpe d'or")),
      argumentSet("its first twelve digits answering none", "978201210134", emptyList<String>()),
    )

    val MISSPELLED = listOf(
      argumentSet("a word misspelled", "asterik", listOf("La serpe d'or", "Astérix et les Goths"), true),
      argumentSet("no edition near either", "gaston", emptyList<String>(), false),
    )

    val LA_SERPE_D_OR = asterix("9782012101340", "La serpe d'or", 2)
    val ASTERIX_ET_LES_GOTHS = asterix("9782012101357", "Astérix et les Goths", 3)
    val ONE_PIECE_1 = onePiece("9782723488525", "Romance dawn", 1)
    val ONE_PIECE_2 = onePiece("9782723489898", "Aux prises avec Baggy et ses hommes", 2)
    val ONE_PIECE_12 = onePiece("9782723492577", "La légende est en marche", 12)
    val ONE_PIECE_20 = onePiece("9782723494762", "Bataille décisive à Alubarna", 20)
    val LE_LOTUS_BLEU = NewBook(
      isbn13 = "9782203001046",
      kind = "BD",
      title = "Le Lotus bleu",
      authors = listOf(Author("Hergé", "WRITER"), Author("Hergé", "ARTIST")),
      series = SeriesEntry("Tintin", 5),
      publisher = "Casterman",
      language = "fr",
    )
    val EMILE = NewBook(
      isbn13 = "9782013224895",
      kind = "BOOK",
      title = "Émile et les détectives",
      authors = listOf(Author("Erich Kästner", "WRITER")),
      language = "fr",
    )

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

    fun onePiece(isbn: String, title: String, volume: Int) =
      NewBook(
        isbn13 = isbn,
        kind = "MANGA",
        title = title,
        authors = listOf(Author("Eiichirō Oda", "WRITER"), Author("Eiichirō Oda", "ARTIST")),
        series = SeriesEntry("One piece", volume),
        publisher = "Glénat",
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
