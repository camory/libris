package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.bibliography.fixture.recordedBytes
import fr.amory.libris.scenario.dsl.Author
import fr.amory.libris.scenario.dsl.Bookshelf
import fr.amory.libris.scenario.dsl.Copy
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.NewBook
import fr.amory.libris.scenario.dsl.Reader
import fr.amory.libris.scenario.dsl.Series
import fr.amory.libris.scenario.dsl.adds
import fr.amory.libris.scenario.dsl.asks
import fr.amory.libris.scenario.dsl.awaitsCoverOf
import fr.amory.libris.scenario.dsl.hasAdded
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.web.servlet.client.RestTestClient

@ScenarioTest
class BookshelfScenarios @Autowired constructor(
  http: RestTestClient,
  @Qualifier("bnf") bnfServer: WireMockServer,
  @Qualifier("openLibrary") openLibraryServer: WireMockServer) {
  private val libris = Libris(http)
  private val bnf = BnfStubs(bnfServer)
  private val openLibrary = OpenLibraryStubs(openLibraryServer)
  private val sources = listOf(bnfServer, openLibraryServer)

  @Test
  fun `S1 The first visit creates the bookshelf`() {
    // Given
    val lea = libris.reader("lea", "Léa")

    // When
    val answer = lea.visits()

    // Then
    answer.value.defaultBookshelf.id.shouldNotBeEmpty()
    answer.value.defaultBookshelf.name shouldBe "Bibliothèque de Léa"
  }

  @Test
  fun `S2 The ouvrage is added`() {
    // Given
    val juliette = libris.reader("juliette", "Juliette")
    bnf.knows("9782723488525")
    juliette.asks("9782723488525").value.copies shouldBe emptyList()
    val bookshelf = juliette.defaultBookshelf

    // When
    val answer = juliette.adds(NEW_ONE_PIECE_1, to = bookshelf)

    // Then
    val copy = answer.value
    copy.bookshelf shouldBe Bookshelf(bookshelf.id, "Bibliothèque de Juliette")
    val card = juliette.asks("9782723488525").value
    card.kind shouldBe "MANGA"
    card.copies shouldBe listOf(copy)
  }

  @Test
  fun `S3 A known ISBN reaches the existing edition, another reader`() {
    // Given
    val marc = libris.reader("marc", "Marc")
    val nina = libris.reader("nina", "Nina")
    marc.hasAdded(NEW_ONE_PIECE_2, to = marc.defaultBookshelf)

    // When
    val answer = nina.adds(NEW_ONE_PIECE_2, to = nina.defaultBookshelf)

    // Then
    val copy = answer.value
    copy.bookshelf.name shouldBe "Bibliothèque de Nina"
    val card = nina.asks("9782723489898").value
    card.title shouldBe "Aux prises avec Baggy et ses hommes"
    card.copies shouldBe listOf(copy)
  }

  @Test
  fun `S3 A known ISBN reaches the existing edition, the same reader again`() {
    // Given
    val paul = libris.reader("paul", "Paul")
    val bookshelf = paul.defaultBookshelf
    val first = paul.hasAdded(NEW_ONE_PIECE_2, to = bookshelf)

    // When
    val answer = paul.adds(NEW_ONE_PIECE_2, to = bookshelf)

    // Then
    answer.value.id shouldNotBe first.id
    paul.asks("9782723489898").value.copies.map { it.bookshelf.name } shouldBe
      listOf("Bibliothèque de Paul", "Bibliothèque de Paul")
  }

  @Test
  fun `S4 The ouvrage is already in a bookshelf`() {
    // Given
    val rose = libris.reader("rose", "Rose")
    val sam = libris.reader("sam", "Sam")
    val rosesCopy = addedWithItsCover(rose, NEW_ONE_PIECE_3)
    sam.hasAdded(NEW_ONE_PIECE_3, to = sam.defaultBookshelf)

    // When
    val answer = rose.asks(ONE_PIECE_3)

    // Then
    val card = answer.value
    card.title shouldBe "Piège"
    card.kind shouldBe "MANGA"
    card.copies shouldBe listOf(rosesCopy)
    rosesCopy.bookshelf.name shouldBe "Bibliothèque de Rose"
    noSourceWasAsked()
  }

  @Test
  fun `S4 The ouvrage is already in a bookshelf, none of the reader's`() {
    // Given
    val tom = libris.reader("tom", "Tom")
    val zoe = libris.reader("zoe", "Zoé")
    addedWithItsCover(tom, NEW_ONE_PIECE_3)

    // When
    val answer = zoe.asks(ONE_PIECE_3)

    // Then
    val card = answer.value
    card.title shouldBe "Piège"
    card.copies shouldBe emptyList()
    noSourceWasAsked()
  }

  private fun addedWithItsCover(reader: Reader, book: NewBook): Copy {
    openLibrary.hasCover(book.isbn13, TALL_JPEG)
    val copy = reader.hasAdded(book, to = reader.defaultBookshelf)
    reader.awaitsCoverOf(book.isbn13)
    sources.forEach { it.resetRequests() }
    return copy
  }

  private fun noSourceWasAsked() {
    sources.forEach { it.verify(0, anyRequestedFor(anyUrl())) }
  }

  private companion object {
    const val ONE_PIECE_3 = "9782723489904"
    val TALL_JPEG = recordedBytes("covers/tall.jpg")
    val NEW_ONE_PIECE_1 = onePiece(
      isbn = "9782723488525",
      title = "Romance dawn",
      subtitle = "à l'aube d'une grande aventure",
      volume = 1,
    )
    val NEW_ONE_PIECE_2 = onePiece(
      isbn = "9782723489898",
      title = "Aux prises avec Baggy et ses hommes",
      subtitle = null,
      volume = 2,
    )
    val NEW_ONE_PIECE_3 = onePiece(
      isbn = ONE_PIECE_3,
      title = "Piège",
      subtitle = null,
      volume = 3,
      coverSource = "Open Library",
    )

    fun onePiece(isbn: String, title: String, subtitle: String?, volume: Int, coverSource: String? = null) =
      NewBook(
        isbn13 = isbn,
        kind = "MANGA",
        title = title,
        subtitle = subtitle,
        authors = listOf(Author("Eiichirō Oda", "WRITER"), Author("Eiichirō Oda", "ARTIST")),
        series = Series("One piece", volume),
        collection = "Shonen manga",
        publisher = "Glénat",
        publicationYear = 2013,
        language = "fr",
        coverSource = coverSource,
      )
  }
}
